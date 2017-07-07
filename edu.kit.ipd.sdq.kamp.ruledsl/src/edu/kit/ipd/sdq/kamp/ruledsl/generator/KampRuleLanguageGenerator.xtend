package edu.kit.ipd.sdq.kamp.ruledsl.generator

import com.google.inject.Inject
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
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.FileLocator
import java.net.URISyntaxException

// TODO support reload and exceptions
// TODO load imports from .karl dynamically
class KampRuleLanguageGenerator implements IGenerator {
	
	@Inject
	JvmModelGenerator jvmModelGenerator;
	
	val IWorkspace workspace = ResourcesPlugin.getWorkspace();
    val IWorkspaceRoot root = workspace.getRoot();
	
	public static final String BUNDLE_NAME = "edu.kit.ipd.sdq.kamp.ruledsl.lookup.bundle";
        
	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		// delegate to Java generator
        jvmModelGenerator.doGenerate(resource, fsa);
        
        var String name = "kamp";	// default name is "kamp-rules" otherwise we take the KAMP project's name whose .karl file triggered the build
       	var URI uri;
       	var JvmDeclaredType rootResource;
       	var String jFileName;
       	
       	// get root element for name
       	for (obj : resource.contents) {
			if(obj instanceof JvmDeclaredType) {
				rootResource = obj;
			}
		}
		
		if(rootResource == null) {
			println("No JvmDeclaredType found. Quit.")
			return;	// nothing to do here
		}
       	
        if(fsa instanceof IFileSystemAccessExtension2) {
        	jFileName = rootResource.qualifiedName.replace('.', '/') + '.java';
        	uri = fsa.getURI(jFileName);
        	println("Generated file is located under: " + uri);
        	name = fsa.getURI("").path.replace("/resource/", "").replace("/src-gen", "");
        } else {
        	throw new UnsupportedOperationException("Wrong FileSystemAccess assigned by xText.");
        }
        
        val causingProjectName = name;
        val URI sourceFileUri = uri;
        val String javaFileName = jFileName;
        
		val String mainName = "Insert new rules for " + causingProjectName;
		var Job job = new Job(mainName) {
			
			override protected run(IProgressMonitor monitor) {
				val SubMonitor subMonitor = SubMonitor.convert(monitor, mainName, IProgressMonitor.UNKNOWN);
				
				monitor.subTask("Create source code project")
			   	val IProject project = createProject(subMonitor.split(1), causingProjectName);
			   	moveRuleSourceFile(subMonitor.split(1), project, sourceFileUri, javaFileName);
			   	buildProject(project, subMonitor.split(1));
			   	installProjectBundle(project)
			   	monitor.done
			   
			   Status.OK_STATUS;
			}
	
		};
		job.setUser(true);
		job.schedule();
	}	
	
		
	def installProjectBundle(IProject project) {
		val BundleContext bundlecontext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	    val Bundle b = bundlecontext.installBundle("file:C:\\Users\\Martin Löper\\KAMP Projekt\\runtime-New_configuration\\MartinTest1-rules\\bin");
	    b.start();
	    
	    /* lookup the kamp dsl bundle */
	   for(bundle : bundlecontext.bundles) {
	   	 if(bundle.symbolicName.equals(BUNDLE_NAME)) {
	   	 	// call method on bundle
	   	 }
	   }
	}
	
	def buildProject(IProject project, IProgressMonitor monitor) {
		project.build(IncrementalProjectBuilder.AUTO_BUILD, monitor);
	}
	
	def moveRuleSourceFile(SubMonitor monitor, IProject destinationProject, URI sourceFile, String jFileName) {		
		val workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		val sourcePath = new Path(workspaceLocation.toOSString + File.separator + sourceFile.toPlatformString(false));
		val File srcFile = sourcePath.toFile
		
		destinationProject.getFile(jFileName).create(new FileInputStream(srcFile), false, monitor)
	}	
	
	// see: https://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically
	def createProject(IProgressMonitor progressMonitor, String name) {
		var IProject compilerProject = root.getProject(name + "-rules")
				
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
			
			
			val String projectName = BUNDLE_NAME;
			val Set<String> requiredBundles = newHashSet
			val List<String> exportedPackages = newArrayList
			val List<String> srcFolders = newArrayList
			
			// export the default bundle
			exportedPackages.add( "." )
			
			// search for imported models
			var registry = EPackage.Registry.INSTANCE;
			for (String key : new HashSet<String>(registry.keySet())) {
			    var ePackage = registry.getEPackage(key);
			    // TODO request dynamically from import statements
			    if(ePackage.nsURI.equals("http://palladiosimulator.org/PalladioComponentModel/5.1")) {
			    	// taken from MirBaseQuickFixProvider
			    	val String contributorName = EclipseBridge.getNameOfContributorOfExtension("org.eclipse.emf.ecore.generated_package", "uri", ePackage.nsURI)
			   		requiredBundles.add(contributorName)
			    }
			}
			
			// for service interface reference
			requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl");
			
			srcFolders.add(".")
			
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
						
			// set source folder
//			val IFolder sourceFolder = compilerProject.getFolder("src");
//			sourceFolder.create(false, true, progressMonitor);

			// is the root folder already a source folder?
//			val IPackageFragmentRoot root = compilerJavaProject.getPackageFragmentRoot(sourceFolder);
//			val IClasspathEntry[] oldEntries = compilerJavaProject.getRawClasspath();
//			val IClasspathEntry[] newEntries = newArrayOfSize(oldEntries.length + 1);
//			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
//			newEntries.set(oldEntries.length, JavaCore.newSourceEntry(root.getPath(), null));
//			compilerJavaProject.setRawClasspath(newEntries, progressMonitor);

			createActivator(compilerProject, progressMonitor)
			createService(compilerProject, progressMonitor)
		}
		
		return compilerProject
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
		pluginProject.getFile("Activator.java").create(stream, false, monitor)
    }
    
    def createService(IProject pluginProject, IProgressMonitor monitor) {
        val Bundle bundle = FrameworkUtil.getBundle(class);
		val InputStream stream = bundle.getEntry("resources/RuleProviderImpl.java").openStream;
		pluginProject.getFile("RuleProviderImpl.java").create(stream, false, monitor)
    }
}