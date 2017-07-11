package edu.kit.ipd.sdq.kamp.ruledsl.generator

import com.google.inject.Inject
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import java.util.List
import java.util.Set
import java.util.stream.Collectors
import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
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
import org.osgi.framework.FrameworkUtil
import tools.vitruv.framework.util.bridges.EclipseBridge

import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.*
import java.io.FileNotFoundException

class KampRuleLanguageGenerator implements IGenerator {
	
	@Inject JvmModelGenerator jvmModelGenerator;
	
	// if set true, project does not get delete if an error occurs
	private static final boolean DEBUG = true;
	
       
	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		// delegate to Java generator in order to generate rule class files
		jvmModelGenerator.doGenerate(resource, fsa)
					
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
        			val cFileName = res.simpleName.replace('.', '/') + '.java';        			
		        	jFileNames.add(cFileName);
		        	val cUri = fsa.getURI("gen/rule/" + cFileName);
		        	uris.add(cUri);
		        	println("Generated file is located under: " + cUri);
		        	
		        	// FIXME this is actually a very unstable implementation. Look for a better way to find the project where the karl file is located
		        	val path = root.getFile(new Path(fsa.getURI("").toPlatformString(true)))
		        	name = path.parent.name
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
		val String RULE_JOB_FAMILY = "KAMP_RULE_CREATION_JOB";
		
		var Job job = new Job(mainName) {			
		    override belongsTo(Object family)
		    {
		        return RULE_JOB_FAMILY.equals(family);
		    }
			
			override protected run(IProgressMonitor mon) {
				val boolean reload = root.getProject(causingProjectName + "-rules").exists;
				val SubMonitor subMonitor = SubMonitor.convert(mon, mainName, 18);
					
				try {
				   	var IProject project;
				   	
				   	if(reload) {
				   		project = getProject(causingProjectName)
				   		removeGeneratedFolderContents(project, subMonitor.split(1));
				   	} else {
				   		subMonitor.split(1).beginTask("Create plugin project", 1);
				   		project = createProject(subMonitor.split(1), causingProjectName);
				   		createService(project, subMonitor.split(1))
				   		createPluginXml(project, subMonitor.split(1));
				   	}
				   		
				    createManifest(getBundleNameForProjectName(causingProjectName), project, packageUris, subMonitor.split(1))			   	
				   	createActivator(project, subMonitor.split(1), ruleFile)
					createServiceBase(project, subMonitor.split(1));
					createStartupRegistry(project, subMonitor.split(1));
				   	
				   	// the following line is not needed anymore as we have a custom FileSystemAccess right now
				   	moveRuleSourceFiles(subMonitor.split(1), project, sourceFileUris, javaFileNames);
				  
				   	if(!reload) {
				  	 	setupProject(subMonitor.split(3), project)
				   	}
				   	
				   	buildProject(project, subMonitor.split(1));
				   	project.refreshLocal(IProject.DEPTH_INFINITE, subMonitor.split(1))
				   	
				   	val Bundle dslBundle = getDslBundle(causingProjectName);
				   	if(dslBundle !== null) {
				   		registerProjectBundle(project, dslBundle, subMonitor.split(2))
				   	} else {
				   		installAndStartProjectBundle(project, subMonitor.split(1))
				   	}
				   	
				   	println("DONE")
				   	 
				   	mon.done
				   
				    Status.OK_STATUS
				} catch(Exception e) {
					// remove project if build was interrupted
					if(!reload && !DEBUG) {
						removeProject(getProject(causingProjectName), subMonitor);
					}
					return new Status(Status.ERROR, BUNDLE_NAME, "Die Regeln konnten nicht eingefügt werden.", e);
				}
			}	
		};
		
		// reserve exclusive write access to the project... should be done but we get sync issues here
		// job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		
		// show progress dialog for project creation only
		if(!root.getProject(causingProjectName + "-rules").exists)
			job.setUser(true);
		
		// we do not want multiple jobs being running in parallel because we get file locking issues by the JDT plugin			
		if(Job.getJobManager().find(RULE_JOB_FAMILY).size == 0) {
			System.err.println("Starting rule creation job...")
			job.schedule();
		} else {
			System.err.println("Cannot start rule creation job, because there is already one running.")
		}
	}	
	
	def moveRuleSourceFiles(IProgressMonitor monitor, IProject destinationProject, URI[] sourceFiles, String[] jFileNames) {				
		for(var int i = 0; i < sourceFiles.size; i++) {
			val sourceFile = sourceFiles.get(i)
			val jFileName = jFileNames.get(i)
			val workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			val sourcePath = new Path(workspaceLocation.toOSString + File.separator + sourceFile.toPlatformString(false));
			val File srcFile = sourcePath.toFile
			// TODO check if user removed gen folder??
			val IFolder genFolder = destinationProject.getFolder("gen");
			val IFolder ruleFolder = genFolder.getFolder("rule");
			
			if(!ruleFolder.exists) {
				ruleFolder.create(true, false, monitor);
			}
			
			val IFile cFile = ruleFolder.getFile(jFileName);
			
			// delete file if present
			if(cFile.exists) {
				cFile.delete(true, monitor);
			}
			
			cFile.create(new FileInputStream(srcFile), false, monitor)
		}
	}	
	
	def createManifest(String projectName, IProject project, Set<String> packageUris,  IProgressMonitor monitor) {
			val Set<String> requiredBundles = newHashSet
			val Set<String> importedPackages = newHashSet
			val List<String> exportedPackages = newArrayList

			// export the default bundle
			exportedPackages.add( "." )
			
			// search for imported models
			var registry = EPackage.Registry.INSTANCE;
			for (String nsUri : new HashSet<String>(registry.keySet())) {
//				try {
//				    var ePackage = registry.getEPackage(key);
				    for(packageUri : packageUris) {
					    if(nsUri.equals(packageUri)) {
//					    	// taken from MirBaseQuickFixProvider
					    	val String contributorName = EclipseBridge.getNameOfContributorOfExtension("org.eclipse.emf.ecore.generated_package", "uri", nsUri)
					   		if(contributorName !== null) 
					   			requiredBundles.add(contributorName)
					    }
				    }
//				} catch(NoClassDefFoundError e) { } catch(ExceptionInInitializerError e2) {};
			}
			
			requiredBundles.add("org.eclipse.ui");
			requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl");
			requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl.ui")
			//requiredBundles.add("edu.kit.ipd.sdq.kamp.model.modificationmarks")
			//requiredBundles.add("edu.kit.ipd.sdq.kamp4is")
			//requiredBundles.add("edu.kit.ipd.sdq.kamp4is.model.modificationmarks")
			//requiredBundles.add("edu.kit.ipd.sdq.kamp")
			//requiredBundles.add("edu.kit.ipd.sdq.kamp4bp")
			
			// add the kamp packages here because we should resolve the bin folder dependencies via import not require
			importedPackages.add("edu.kit.ipd.sdq.kamp4bp.ruledsl.support");
			importedPackages.add("edu.kit.ipd.sdq.kamp.ruledsl.support");
			importedPackages.add("edu.kit.ipd.sdq.kamp4bp.core");	
			importedPackages.add("edu.kit.ipd.sdq.kamp4is.core")
			importedPackages.add("edu.kit.ipd.sdq.kamp4is.model.modificationmarks")
			
			createManifest(projectName, requiredBundles, importedPackages, exportedPackages, monitor, project);
	}
	
	def createProject(IProgressMonitor progressMonitor, String name) {
		var IProject compilerProject = getProject(name)
				
		// if does not exist, create it
		compilerProject.create(progressMonitor)
		compilerProject.open(progressMonitor)	

		// create the folder
		val IFolder sourceFolder = compilerProject.getFolder("src");
		sourceFolder.create(false, true, progressMonitor);
		
		val IFolder genFolder = compilerProject.getFolder("gen");
		genFolder.create(false, true, progressMonitor);
		
		val IFolder metaFolder = compilerProject.getFolder("META-INF");
		if(!metaFolder.exists)
			metaFolder.create(false, true, progressMonitor);
			
		val IFolder binFolder = compilerProject.getFolder("bin");
		if(!binFolder.exists)
			binFolder.create(false, true, null);
		
		return compilerProject
	}
	
	// see: https://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically
	def setupProject(IProgressMonitor progressMonitor, IProject compilerProject) {		

		// add Java nature
		val IProjectDescription description = compilerProject.getDescription();
		val List<String> newNatures = newArrayList
		newNatures.add(JavaCore.NATURE_ID)
		newNatures.add(IBundleProjectDescription.PLUGIN_NATURE)
		description.setNatureIds(newNatures);

		// init PDE stuffe / set up Eclipse Plugin Project nature
		// source: http://sodecon.blogspot.de/2009/09/create-elicpse-plug-in-project.html
		
		val ICommand java = description.newCommand();
		java.setBuilderName(JavaCore.BUILDER_ID);
		
		val ICommand manifest = description.newCommand();
		manifest.setBuilderName("org.eclipse.pde.ManifestBuilder");

		val ICommand schema = description.newCommand();
        schema.setBuilderName("org.eclipse.pde.SchemaBuilder");
        
        val ICommand customSourceBuilder = description.newCommand;
        customSourceBuilder.builderName = "edu.kit.ipd.sdq.kamp.ruledsl.ui.sourceBuilder"
        
        description.buildSpec = #[java, manifest, schema, customSourceBuilder]
		compilerProject.setDescription(description, progressMonitor);
		
		val List<String> srcFolders = newArrayList
		
		srcFolders.add("src");
		srcFolders.add("gen");
		
		createBuildProps(progressMonitor, compilerProject, srcFolders);
		compilerProject.close(progressMonitor)	// do this to release resources before accessing pdt
		
		compilerProject.open(progressMonitor)
		val IJavaProject compilerJavaProject = JavaCore.create(compilerProject);
		
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
		
		try {
			compilerJavaProject.setRawClasspath(entries, progressMonitor);
		} catch(Exception e) {
			// on windows systems the file locking might prevent from update external folders, but we do not need this
			e.printStackTrace
		}
					
		// set bin folder
		val IFolder binFolder = compilerProject.getFolder("bin");
		if(!binFolder.exists)
			binFolder.create(false, true, null);
			
		compilerJavaProject.setOutputLocation(binFolder.getFullPath(), progressMonitor);			
	
		return compilerProject
	}
	
	def createPluginXml(IProject project, IProgressMonitor progressMonitor) {
		val String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<?eclipse version=\"3.4\"?>\n"
			+ "<plugin>\n"
			+  "<extension point=\"org.eclipse.ui.startup\">\n"
			+      "<startup class=\"gen.Startup\">\n"
			+      "</startup>\n"
			+   "</extension>\n"
//			+ "<extension\n"
//       		+		"id=\"edu.kit.ipd.sdq.kamp.ruledsl.ui.sourceBuilder\"\n"
//      		+	 	"name=\"Kamp Rule Source Code Builder\"\n"
//       		+		"point=\"org.eclipse.core.resources.builders\">\n"
//      		+			"<builder\n"
//            +				"callOnEmptyDelta=\"false\"\n"
//            +				"hasNature=\"false\"\n"
//            +				"isConfigurable=\"false\">\n"
//         	+				"<run class=\"edu.kit.ipd.sdq.kamp.ruledsl.ui.DslJavaSourceBuilder\"></run>\n"
//      		+			"</builder>\n"
//   			+	"</extension>\n"
//			+ "<extension point=\"org.eclipse.ui.popupMenus\">\n"
//      		+	"<objectContribution\n"
//            +		"adaptable=\"true\"\n"
//            + 		"id=\"edu.kit.ipd.sdq.kamp.ruledsl.objectContribution\"\n"
//            +		"objectClass=\"org.eclipse.core.resources.IContainer\">\n"
//         	+			"<action\n"
//            +  				"class=\"edu.kit.ipd.sdq.kamp.ruledsl.ui.RegisterDslBundleAction\"\n"
//            + 				"id=\"edu.kit.ipd.sdq.kamp.ruledsl.actionRegister\"\n"
//            +				"label=\"Generate Rules Definition File\"\n"
//            +				"menubarPath=\"kamp\"\n"
//            +			    "enablesFor=\"1\">\n"
//         	+			"</action>\n"
//      		+		"</objectContribution>\n"
//    		+	"</extension>\n"
			+ "</plugin>";
		
		createFile("plugin.xml", project, template, progressMonitor);		
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

	def createManifest(String projectName, Set<String> requiredBundles,  Set<String> importedPackages,
			List<String> exportedPackages, IProgressMonitor progressMonitor, IProject project)
	throws CoreException {
		val StringBuilder maniContent = new StringBuilder("Manifest-Version: 1.0\n");
		maniContent.append("Bundle-ManifestVersion: 2\n");
		maniContent.append("Bundle-Name: " + projectName + "\n");
		maniContent.append("Bundle-SymbolicName: " + projectName + "; singleton:=true\n");
		maniContent.append("Bundle-Version: 1.0.0\n");
		maniContent.append("Bundle-Vendor: Martin Loeper (KIT)\n");
		// localization not needed, english is the way to go...
		// maniContent.append("Bundle-Localization: plugin\n");
		if(requiredBundles.size > 0)
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
		maniContent.append("Bundle-Activator: gen.Activator\r\n")
		maniContent.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\r\n");
		
		// add package imports
		if(importedPackages.size > 0)
			maniContent.append("Import-Package: ");
		var int k = 0;
		for (String entry : importedPackages) {
			if(k != 0) {
				maniContent.append(",");
			}
			maniContent.append(entry);
			k++;
		}
		maniContent.append("\n");
		
		var IFolder metaInf = project.getFolder("META-INF");
		if(!metaInf.exists) {
			metaInf.create(false, true, progressMonitor);
		}
		
		if(metaInf.getFile("MANIFEST.MF").exists) {
			metaInf.getFile("MANIFEST.MF").delete(true, progressMonitor)
		}
			
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
	
	def createActivator(IProject pluginProject, IProgressMonitor monitor, RuleFile ruleFile) {
		// determine classes to be registered
		var String rulesToBeRegistered = "";
		val rules = ruleFile.rules;
		for(var int i = 0; i < rules.size; i++) {
			if(i > 0) {
				rulesToBeRegistered += ", ";
			}
			rulesToBeRegistered += rules.get(i).name.toFirstUpper + "Rule.class";
		}
		
		// copy template and fill in classes to be registered
        val Bundle bundle = FrameworkUtil.getBundle(class);
		var InputStream in = null;
		
		try {
			in = bundle.getEntry("resources/Activator.java").openStream;
	
	        var String res = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
			res = String.format(res, rulesToBeRegistered)
	        		
			pluginProject.getFolder("gen").getFile("Activator.java").create(new ByteArrayInputStream(res.getBytes(StandardCharsets.UTF_8)), true, monitor)
    	} finally {
    		try { if(in !== null) in.close(); } finally {}
    	}	
    }
    
    def createStartupRegistry(IProject pluginProject, IProgressMonitor monitor) {
    	val Bundle bundle = FrameworkUtil.getBundle(class);
        var InputStream in = null;
		try {
			in = bundle.getEntry("resources/Startup.java").openStream;        
			pluginProject.getFolder("gen").getFile("Startup.java").create(in, false, monitor)
		} finally {
			try { if(in !== null) in.close(); } finally {}
		}
    }
    
    def createServiceBase(IProject pluginProject, IProgressMonitor monitor) {
    	val Bundle bundle = FrameworkUtil.getBundle(class);
        var InputStream in = null;
		try {
			in = bundle.getEntry("resources/RuleProviderBase.java").openStream;        
			pluginProject.getFolder("gen").getFile("RuleProviderBase.java").create(in, false, monitor)
		} finally {
			try { if(in !== null) in.close(); } finally {}
		}
    }
    
    def createService(IProject pluginProject, IProgressMonitor monitor) {
        val Bundle bundle = FrameworkUtil.getBundle(class);
        var InputStream in = null;
		try {
			in = bundle.getEntry("resources/RuleProviderImpl.java").openStream;        
			pluginProject.getFolder("src").getFile("RuleProviderImpl.java").create(in, false, monitor)
		} finally {
			try { if(in !== null) in.close(); } finally {}
		}
    }
    
    def removeGeneratedFolderContents(IProject destinationProject, IProgressMonitor monitor) {
		if(!destinationProject.getFolder("gen").exists)
			return;
			
		for(res : destinationProject.getFolder("gen").members) {
			res.delete(true, monitor)
		}
	}
	
	def removeProject(IProject project, IProgressMonitor monitor) {
		project.delete(true, true, monitor);
	}
}