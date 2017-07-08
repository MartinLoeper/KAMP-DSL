package edu.kit.ipd.sdq.kamp.ruledsl.generator

import com.google.inject.Inject
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Arrays
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.SubProgressMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.IVMInstall
import org.eclipse.jdt.launching.JavaRuntime
import org.eclipse.jdt.launching.LibraryLocation
import org.eclipse.pde.core.project.IBundleProjectDescription
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IFileSystemAccessExtension2
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil
import tools.vitruv.framework.util.bridges.EclipseBridge
import org.eclipse.emf.ecore.EObject
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRule
import java.util.ArrayList
import java.util.Base64
import java.util.Base64.Encoder
import org.eclipse.jdt.core.IPackageFragmentRoot
import java.util.Iterator
import org.eclipse.core.resources.IResource

class KampRuleLanguageGenerator implements IGenerator {
	
	@Inject
	JvmModelGenerator jvmModelGenerator;
	
	static val IWorkspace workspace = ResourcesPlugin.getWorkspace();
    static val IWorkspaceRoot root = workspace.getRoot();
	
	// TODO bind to project name
	public static final String BUNDLE_NAME = "edu.kit.ipd.sdq.kamp.ruledsl.lookup.bundle";
        
	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		// delegate to Java generator
        jvmModelGenerator.doGenerate(resource, fsa);
        
        var String name = "kamp";	// default name is "kamp-rules" otherwise we take the KAMP project's name whose .karl file triggered the build
       	var List<URI> uris = newArrayList();
       	var List decTypes = new ArrayList();
       	var List<String> jFileNames = newArrayList();
       	
       	// get root element for name
       	for (obj : resource.contents) {
			if(obj instanceof JvmDeclaredType) {
				decTypes.add(obj)
			}
		}
		
		if(decTypes.size == 0) {
			println("No JvmDeclaredType found. Quit.")
			return;	// nothing to do here
		}
       	
       	// copy generated files to project folder
        if(fsa instanceof IFileSystemAccessExtension2) {
        	for(res : decTypes) {
        		if(res instanceof JvmDeclaredType) {
        			val cFileName = res.qualifiedName.replace('.', '/') + '.java';
		        	jFileNames.add(cFileName);
		        	val cUri = fsa.getURI(cFileName);
		        	uris.add(cUri);
		        	println("Generated file is located under: " + cUri);
		        	
		        	// FIXME this is actually a very unstable implementation. Look for a better way to find the project where the karl file is located
		        	name = fsa.getURI("").path.replace("/resource/", "").replace("/src-gen", "");
	        	}
        	}
        } else {
        	throw new IllegalStateException("Wrong FileSystemAccess assigned by xText.");
        }
        
        /*
         * get import statements
         */
		val ruleFiles = resource.contents.filter[elist | elist instanceof RuleFile]
		if(ruleFiles.empty) {
			// TODO handle properly
			throw new IllegalStateException("No RuleFile present in input file.");
		}
		val RuleFile ruleFile = ruleFiles.head as RuleFile
		val imports = ruleFile.metamodelImports
		
		val packageUris = newHashSet
		for(metamodellImport : imports) {
			val package = metamodellImport.package as EPackage
			packageUris.add(package.nsURI)
		}
        
        val causingProjectName = name;
        val URI[] sourceFileUris = uris;
        val String[] javaFileNames = jFileNames;
        
		val String mainName = "Insert new rules for " + causingProjectName;
		var Job job = new Job(mainName) {
			
			override protected run(IProgressMonitor monitor) {
				val boolean reload = root.getProject(causingProjectName + "-rules").exists;
				val SubMonitor subMonitor = SubMonitor.convert(monitor, mainName, IProgressMonitor.UNKNOWN);
					
				try {
				   	var IProject project;
				   	
				   	if(reload) {
				   		project = getProject(causingProjectName)
				   		removeGeneratedFolderContents(project, subMonitor.split(1));
				   	} else {
				   		project = createProject(subMonitor.split(1), causingProjectName, packageUris);
				   	}
				   	
				   	moveRuleSourceFiles(subMonitor.split(1), project, sourceFileUris, javaFileNames);
				   	buildProject(project, subMonitor.split(1));
				   	
				   	val Bundle dslBundle = getDslBundle(causingProjectName);
				   	if(dslBundle !== null) {
				   		registerProjectBundle(project, dslBundle)
				   	} else {
				   		installAndStartProjectBundle(project)
				   	}
				   	
				   	monitor.done
				   
				    Status.OK_STATUS
				} catch(Exception e) {
					// remove project if build was interrupted
					if(!reload) {
						removeProject(getProject(causingProjectName), subMonitor);
					}
					return new Status(Status.ERROR, BUNDLE_NAME, "Die Regeln konnten nicht eingefügt werden.", e);
				}
			}	
		};
		// job.setUser(true);
		job.schedule();
	}	
	
	static def getDslBundle(String projectName) {
		val BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator).getBundleContext();
	   	var Bundle cBundle = null;
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName() !== null && bundle.getSymbolicName().equals(KampRuleLanguageGenerator.getBundleNameForProjectName(projectName))) {
	   	  		cBundle = bundle;
	   	  	}
	    }
	    
	    return cBundle
	}
	
	// unregisteres the given bundle if available and registers it again
	static def registerProjectBundle(IProject project, Bundle bundle) {
	    // if there was a bundle registered, wait for the shutdown
	    if(bundle !== null) {
	    	println("DSL Bundle found, uninstalling...")
	    	bundle.uninstall
	    	// is busy wait ok here as we are in a job?
	    	while(bundle.state != Bundle.UNINSTALLED) {};
	    	println("Bundle is finally uninstalled!")
	    }
	    
	    println("Installing new bundle version...")
	    installAndStartProjectBundle(project)
	}
	
	// convenice method for external calls
	// returns null if project does not exist, returns installed bundle otherwise
	static def installAndStartProjectBundle(String callerProjectName) {
		val IProject project = getProject(callerProjectName);
		if(!project.exists) {
			return null;	
		}
		
		installAndStartProjectBundle(project)
	}
		
	static def installAndStartProjectBundle(IProject project) {
		// TODO get name by workspac methods
		val BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageGenerator).getBundleContext();
	    // the bin directory of the plugin, i.e. "file:C:\\Users\\Martin Löper\\KAMP Projekt\\runtime-New_configuration\\MartinTest1-rules\\bin"
	    val folder = project.getFolder("bin")
	    val Bundle b = bundleContext.installBundle("file:" + folder.location.toString);
	    b.start();
	    
	    return b;
	    /* lookup the kamp dsl bundle */
	    // see edu.kit.ipd.sdq.kamp4bp.core#calculateInterBusinessProcessPropagation
//	   for(bundle : bundlecontext.bundles) {
//	   	 if(bundle.symbolicName.equals(BUNDLE_NAME)) {
//	   	 	// call method on bundle
//	   	 }
//	   }
	}
	
	def buildProject(IProject project, IProgressMonitor monitor) {
		project.build(IncrementalProjectBuilder.AUTO_BUILD, monitor);
	}
	
	def moveRuleSourceFiles(IProgressMonitor monitor, IProject destinationProject, URI[] sourceFiles, String[] jFileNames) {				
		for(var int i = 0; i < sourceFiles.size; i++) {
			val sourceFile = sourceFiles.get(i)
			val jFileName = jFileNames.get(i)
			val workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			val sourcePath = new Path(workspaceLocation.toOSString + File.separator + sourceFile.toPlatformString(false));
			val File srcFile = sourcePath.toFile
			// TODO check if user removed folder??
			val IFile cFile = destinationProject.getFolder("gen").getFile(jFileName);
			
			// delete file if present
			if(cFile.exists) {
				cFile.delete(true, monitor);
			}
			
			cFile.create(new FileInputStream(srcFile), false, monitor)
		}
	}	
	
	static def getProject(String causingProjectName) {
		return root.getProject(causingProjectName + "-rules")
	}
	
	// see: https://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically
	def createProject(IProgressMonitor progressMonitor, String name, Set<String> packageUris) {
		var IProject compilerProject = getProject(name)
				
		if (!compilerProject.exists) {
			// if does not exist, create it
			compilerProject.create(progressMonitor)
			compilerProject.open(progressMonitor)	

			// add Java nature
			val IProjectDescription description = compilerProject.getDescription();
			val List<String> newNatures = newArrayList
			newNatures.add(JavaCore.NATURE_ID)
			newNatures.add(IBundleProjectDescription.PLUGIN_NATURE)
			description.setNatureIds(newNatures);
			compilerProject.setDescription(description, progressMonitor);
			
			// init PDE stuffe / set up Eclipse Plugin Project nature
			// source: http://sodecon.blogspot.de/2009/09/create-elicpse-plug-in-project.html
			
			val ICommand java = description.newCommand();
			java.setBuilderName(JavaCore.BUILDER_ID);
			
			val ICommand manifest = description.newCommand();
			manifest.setBuilderName("org.eclipse.pde.ManifestBuilder");

			val ICommand schema = description.newCommand();
            schema.setBuilderName("org.eclipse.pde.SchemaBuilder");
            
            description.buildSpec = #[java, manifest, schema]
			compilerProject.setDescription(description, progressMonitor);
			
			
			val String projectName = getBundleNameForProjectName(name);
			val Set<String> requiredBundles = newHashSet
			val List<String> exportedPackages = newArrayList
			val List<String> srcFolders = newArrayList
			
			// export the default bundle
			exportedPackages.add( "." )
			
			// search for imported models
			var registry = EPackage.Registry.INSTANCE;
			for (String key : new HashSet<String>(registry.keySet())) {
			    var ePackage = registry.getEPackage(key);
			    for(packageUri : packageUris) {
				    if(ePackage.nsURI.equals(packageUri)) {
				    	// taken from MirBaseQuickFixProvider
				    	val String contributorName = EclipseBridge.getNameOfContributorOfExtension("org.eclipse.emf.ecore.generated_package", "uri", ePackage.nsURI)
				   		requiredBundles.add(contributorName)
				    }
			    }
			}
			
			// for service interface reference
			requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl");
			
			srcFolders.add("src");
			srcFolders.add("gen");
			
			createManifest(projectName, requiredBundles, exportedPackages, progressMonitor, compilerProject);
	        createBuildProps(progressMonitor, compilerProject, srcFolders);
			
			val IJavaProject compilerJavaProject = JavaCore.create(compilerProject)
	
			// add JRE to classpath
			var Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
			entries.addAll(Arrays.asList(compilerJavaProject.getRawClasspath()));
			
//			entries.addAll(Arrays.asList(NewJavaProjectPreferencePage.getDefaultJRELibrary()));
			entries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
			
			
			val IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
			val LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);
			
			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			
			compilerJavaProject.setRawClasspath(entries, progressMonitor);
						
			// set bin folder
			val IFolder binFolder = compilerProject.getFolder("bin");
			if(!binFolder.exists)
				binFolder.create(false, true, null);
			compilerJavaProject.setOutputLocation(binFolder.getFullPath(), progressMonitor);			
						
			// create gen and source folder
			val IFolder sourceFolder = compilerProject.getFolder("src");
			sourceFolder.create(false, true, progressMonitor);
			
			val IFolder genFolder = compilerProject.getFolder("gen");
			genFolder.create(false, true, progressMonitor);
			
			// add src and generated as source folders
			val IPackageFragmentRoot srcFolderFrag = compilerJavaProject.getPackageFragmentRoot(sourceFolder);
			val IPackageFragmentRoot genFolderFrag = compilerJavaProject.getPackageFragmentRoot(genFolder);
			var IClasspathEntry[] oldEntries = compilerJavaProject.getRawClasspath();
			val ArrayList<IClasspathEntry> oldEntriesMod = new ArrayList<IClasspathEntry>(oldEntries);	// dirty!... make list changeable
			
			// is the root folder already a source folder? if yes, remove it
			for(val Iterator<IClasspathEntry> it = oldEntriesMod.iterator; it.hasNext;) {
				val entry = it.next
				if(entry.contentKind == IPackageFragmentRoot.K_SOURCE && entry.entryKind == IClasspathEntry.CPE_SOURCE) {
					// this is the source container entry for the project root
					// remove this entry
					it.remove
					
				}
			}
			
			oldEntries = oldEntriesMod;
			
			val IClasspathEntry[] newEntries = newArrayOfSize(oldEntries.length + 2);
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries.set(oldEntries.length, JavaCore.newSourceEntry(srcFolderFrag.getPath(), null));
			newEntries.set(oldEntries.length + 1, JavaCore.newSourceEntry(genFolderFrag.getPath(), null));
			compilerJavaProject.setRawClasspath(newEntries, progressMonitor);

			createActivator(compilerProject, progressMonitor)
			createService(compilerProject, progressMonitor)
		}
		
		return compilerProject
	}
	
	public static def getBundleNameForProjectName(String name) {
		return BUNDLE_NAME + "." + Base64.encoder.withoutPadding.encodeToString(name.bytes)
	}
	
	def createBuildProps(IProgressMonitor progressMonitor, IProject project,
			List<String> srcFolders) {
		val StringBuilder bpContent = new StringBuilder("source.. = ");
		for (var iterator = srcFolders.iterator(); iterator.hasNext();) {
			bpContent.append(iterator.next()).append('/');
			if (iterator.hasNext()) {
				bpContent.append(",");
			}
		}
		bpContent.append("\n");
		bpContent.append("output.. = bin/\n")
		bpContent.append("bin.includes = META-INF/,.\n");

		createFile("build.properties", project, bpContent.toString(), progressMonitor);
	}

	def createManifest(String projectName, Set<String> requiredBundles,
			List<String> exportedPackages, IProgressMonitor progressMonitor, IProject project)
	throws CoreException {
		val StringBuilder maniContent = new StringBuilder("Manifest-Version: 1.0\n");
		maniContent.append("Bundle-ManifestVersion: 2\n");
		maniContent.append("Bundle-Name: " + projectName + "\n");
		maniContent.append("Bundle-SymbolicName: " + projectName + "; singleton:=true\n");
		maniContent.append("Bundle-Version: 1.0.0\n");
		// maniContent.append("Bundle-Localization: plugin\n");
		maniContent.append("Require-Bundle: ");
		var int j = 0;
		for (String entry : requiredBundles) {
			if(j != 0) {
				maniContent.append(",");
			}
			maniContent.append(entry);
			j++;
		}
		maniContent.append("\n");
		if (exportedPackages != null && !exportedPackages.isEmpty()) {
			maniContent.append("Export-Package: " + exportedPackages.get(0));
			for (var i = 1, var x = exportedPackages.size(); i < x; i++) {
				maniContent.append(",\n " + exportedPackages.get(i));
			}
			maniContent.append("\n");
		}
		
		// activate this plugin when one of its classes is loaded
		maniContent.append("Bundle-ActivationPolicy: lazy\r\n");
		maniContent.append("Bundle-Activator: Activator\r\n")
		maniContent.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\r\n");

		var IFolder metaInf = project.getFolder("META-INF");
		metaInf.create(false, true, new SubProgressMonitor(progressMonitor, 1));
		createFile("MANIFEST.MF", metaInf, maniContent.toString(), progressMonitor);
	}
	
	/**
	 * @param name
	 *            of the destination file
	 * @param container
	 *            directory containing the the destination file
	 * @param contentUrl
	 *            Url pointing to the src of the content
	 * @param progressMonitor
	 *            used to interact with and show the user the current operation
	 *            status
	 * @return
	 */
	def IFile createFile(String name, IContainer container, URL contentUrl,
			IProgressMonitor progressMonitor) {

		val IFile file = container.getFile(new Path(name));
		var InputStream inputStream = null;
		try {
			inputStream = contentUrl.openStream();
			if (file.exists()) {
				file.setContents(inputStream, true, true, progressMonitor);
			}
			else {
				file.create(inputStream, true, progressMonitor);
			}
			inputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace
		}
		finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					e.printStackTrace
				}
			}
		}
		progressMonitor.worked(1);

		return file;
	}
	
	def IFile createFile(String name, IContainer container, String content,
			String charSet, IProgressMonitor progressMonitor) throws CoreException {
		val IFile file = createFile(name, container, content, progressMonitor);
		if (file != null && charSet != null) {
			file.setCharset(charSet, progressMonitor);
		}

		return file;
	}
	
	def IFile createFile(String name, IContainer container, String content,
			IProgressMonitor progressMonitor) {
		val IFile file = container.getFile(new Path(name));
		assertExist(file.getParent());
		try {
			val InputStream stream = new ByteArrayInputStream(content.getBytes(file.getCharset()));
			if (file.exists()) {
				file.setContents(stream, true, true, progressMonitor);
			}
			else {
				file.create(stream, true, progressMonitor);
			}
			stream.close();
		}
		catch (Exception e) {
			e.printStackTrace
		}
		progressMonitor.worked(1);

		return file;
	}
	
	def void assertExist(IContainer c) {
		if (!c.exists()) {
			if (!c.getParent().exists()) {
				assertExist(c.getParent());
			}
			if (c instanceof IFolder) {
				try {
					c.create(false, true, new NullProgressMonitor());
				}
				catch (CoreException e) {
					e.printStackTrace
				}
			}

		}
	}
	
	def createActivator(IProject pluginProject, IProgressMonitor monitor) {
        val Bundle bundle = FrameworkUtil.getBundle(class);
		val InputStream stream = bundle.getEntry("resources/Activator.java").openStream;
		pluginProject.getFolder("src").getFile("Activator.java").create(stream, false, monitor)
    }
    
    def createService(IProject pluginProject, IProgressMonitor monitor) {
        val Bundle bundle = FrameworkUtil.getBundle(class);
		val InputStream stream = bundle.getEntry("resources/RuleProviderImpl.java").openStream;
		pluginProject.getFolder("src").getFile("RuleProviderImpl.java").create(stream, false, monitor)
    }
    
    def removeGeneratedFolderContents(IProject destinationProject, IProgressMonitor monitor) {
		for(res : destinationProject.getFolder("gen").members) {
			res.delete(true, monitor)
		}
	}
	
	def removeProject(IProject project, IProgressMonitor monitor) {
		project.delete(true, true, monitor);
	}
}