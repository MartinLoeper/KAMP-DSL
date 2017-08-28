package edu.kit.ipd.sdq.kamp.ruledsl.generator

import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile
import edu.kit.ipd.sdq.kamp.ruledsl.util.ErrorContext
import edu.kit.ipd.sdq.kamp.ruledsl.util.RollbarExceptionReporting
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.HashSet
import java.util.List
import java.util.Set
import java.util.jar.Attributes
import java.util.jar.Manifest
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
import org.eclipse.core.runtime.jobs.IJobManager
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.pde.core.project.IBundleProjectDescription
import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkUtil
import tools.vitruv.framework.util.bridges.EclipseBridge

import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.*

abstract class KarlJobBase extends Job {
	
	// the job family name for karl jobs
	public static String RULE_JOB_FAMILY = "KAMP_RULE_CREATION_JOB";
		
	protected final Configuration config;
	
	new(Configuration config, String name) {
		super(name)
		this.config = config;
	}
	
	override belongsTo(Object family) {
        return RULE_JOB_FAMILY.equals(family);
    }
    
    	// TODO create a dedicated class for job related tasks which apply to both jobs
	def static isKarlJobRunning() {
		val Job[] allJobs = Job.getJobManager().find(RULE_JOB_FAMILY);
		
		return allJobs.length > 0;
	}
	
	def trySchedule() {
		// we do not want multiple jobs being running in parallel because we get file locking issues by the JDT plugin			
		if(Job.getJobManager().find(RULE_JOB_FAMILY).size == 0) {
			System.err.println("Starting rule creation job...")
			schedule();
		} else {
			System.err.println("Cannot start rule creation job, because there is already one running.")
		}
	}
	
	def waitForBuild(IProgressMonitor monitor) {
		// wait for the build to finish
	   	val IJobManager jobManager = Job.getJobManager();
		
		// Wait for manual build to finish if running
		jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
		
		// Wait for auto build to finish if running
		jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
	}
	
		/**
	 * Synchronizes the MANIFEST imports from the KAMP project and the KARL project.
	 * 
	 * @param project the KARL project
	 * @param assignedProjectName the KARL project name
	 */
	def static syncManifests(IProject project, String assignedProjectName) {
		var InputStream is = null;
		var InputStream is2 = null;
		
		try {
			// get the contents of the KARL project's manifest
			is = project.getFolder("META-INF").getFile("MANIFEST.MF").contents
			val Manifest dslManifest = new Manifest(is);
			val importedPackagesDsl = dslManifest.mainAttributes.getValue("Import-Package")?.split(",");
			val requiredBundlesDsl = dslManifest.mainAttributes.getValue("Require-Bundle")?.split(",");

			val parentProject = root.getProject(assignedProjectName);
			if(!parentProject.open) { 
				parentProject.open(new NullProgressMonitor);
			}
			
			// get the contents of the KAMP project's manifest
			is2 = parentProject.getFolder("META-INF").getFile("MANIFEST.MF").contents
			val Manifest parentManifest = new Manifest(is2);
			val importedPackagesParentProject = parentManifest.mainAttributes.getValue("Import-Package")?.split(",");
			val requiredBundlesParentProject = parentManifest.mainAttributes.getValue("Require-Bundle")?.split(",");
			
			// add all imports and requires from KARL project to KAMP project
			val Set<String> mergedReqBundles = new HashSet<String>();
			if(requiredBundlesParentProject !== null) {
				for(String parentEntry : requiredBundlesParentProject) {
					mergedReqBundles.add(new String(parentEntry.bytes, "UTF-8").trim);
				}	
			}	
			
			if(requiredBundlesDsl !== null) {
				for(String dslEntry : requiredBundlesDsl) {
					mergedReqBundles.add(new String(dslEntry.bytes, "UTF-8").trim);
				}	
			}	
			
			val Set<String> mergedImportedPackages = new HashSet<String>();
			if(importedPackagesParentProject !== null) {
				for(String parentEntry : importedPackagesParentProject) {
					mergedImportedPackages.add(new String(parentEntry.bytes, "UTF-8").trim);
				}	
			}	
			
			if(importedPackagesDsl !== null) {
				for(String dslEntry : importedPackagesDsl) {
					mergedImportedPackages.add(new String(dslEntry.bytes, "UTF-8").trim);
				}	
			}
			
			parentManifest.mainAttributes.put(new Attributes.Name("Require-Bundle"), String.join(",", mergedReqBundles));
			parentManifest.mainAttributes.put(new Attributes.Name("Import-Package"), String.join(",", mergedImportedPackages));
			
			var InputStream mis = null;
			try {
				val ByteArrayOutputStream os = new ByteArrayOutputStream();
				parentManifest.write(os);
				val String newManifestAsString = new String(os.toByteArray(), "UTF-8");
				os.close;
				
				mis = new ByteArrayInputStream(newManifestAsString.getBytes(StandardCharsets.UTF_8));
				parentProject.getFolder("META-INF").getFile("MANIFEST.MF").setContents(mis, true, true, new NullProgressMonitor)
			} finally {
				mis?.close
			}
		} finally {
			is?.close;
			is2?.close
		}
	}
	
	def static moveRuleSourceFiles(IProgressMonitor monitor, IProject destinationProject, URI[] sourceFiles, String[] jFileNames) {				
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
	
	def static createManifest(String projectName, IProject project, Set<String> packageUris,  IProgressMonitor monitor, EList<EObject> rootEObject) {
		val Set<String> requiredBundles = newHashSet
		val Set<String> importedPackages = newHashSet
		val List<String> exportedPackages = newArrayList

		// export the default bundle
		exportedPackages.add( "." )
		
		// search for imported models
		var registry = EPackage.Registry.INSTANCE;
		for (String nsUri : new HashSet<String>(registry.keySet())) {
		    for(packageUri : packageUris) {
			    if(nsUri.equals(packageUri)) {
			    	// taken from MirBaseQuickFixProvider
			    	val String contributorName = EclipseBridge.getNameOfContributorOfExtension("org.eclipse.emf.ecore.generated_package", "uri", nsUri)
			   		if(contributorName !== null) 
			   			requiredBundles.add(contributorName)
			    }
		    }
		}
		
		requiredBundles.add("org.eclipse.ui");
		requiredBundles.add("org.eclipse.emf.compare");
		requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl");
		requiredBundles.add("edu.kit.ipd.sdq.kamp.ruledsl.ui")
		//requiredBundles.add("edu.kit.ipd.sdq.kamp.model.modificationmarks")
		//requiredBundles.add("edu.kit.ipd.sdq.kamp4is")
		//requiredBundles.add("edu.kit.ipd.sdq.kamp4is.model.modificationmarks")
		//requiredBundles.add("edu.kit.ipd.sdq.kamp")
		//requiredBundles.add("edu.kit.ipd.sdq.kamp4bp")
		
		// add the kamp packages here because we should resolve the bin folder dependencies via import not require
		// ruledsl
		importedPackages.add("edu.kit.ipd.sdq.kamp.ruledsl.support");
		
		// kamp core
		importedPackages.add("edu.kit.ipd.sdq.kamp.propagation");
		importedPackages.add("edu.kit.ipd.sdq.kamp.architecture")
		importedPackages.add("edu.kit.ipd.sdq.kamp.util");
		importedPackages.add("edu.kit.ipd.sdq.kamp.model.modificationmarks");
		
//		importedPackages.add("edu.kit.ipd.sdq.kamp4bp.core");	
//		importedPackages.add("edu.kit.ipd.sdq.kamp4is.core")
//		importedPackages.add("edu.kit.ipd.sdq.kamp4is.model.modificationmarks")
		
//		val IFolder metaFolder = project.getFolder("META-INF");
//		if(metaFolder.exists) {
//			val IFile metaFile = metaFolder.getFile("MANIFEST.MF"); 
//			if(metaFile.exists) {
//				val is = metaFile.contents;
//				val reader = new BufferedReader(new InputStreamReader(is));
//				try {
//					val String contents = reader.lines().collect(Collectors.joining("\n"));
//					lastLine = contents.substring(contents.lastIndexOf("\n") + 1) + "\n"
//				} finally {
//					if(reader !== null)
//						try { reader.close } catch(IOException e) {}
//						
//					if(is !== null)
//						try { is.close } catch(IOException e) {}
//				}
//			}
//		}

		
		// include the user defined import statements
		if(rootEObject.size > 0) {
			val cRuleFile = rootEObject.get(0);
			if(cRuleFile instanceof RuleFile) {
				for(importStatement : cRuleFile.javaPackageImports) {
					importedPackages.add(importStatement.javaType);
				}
			}
		}

		// include the predefined import statements
		val StringBuilder importStringBuilder = new StringBuilder();
		if(importedPackages.size > 0)
			importStringBuilder.append("Import-Package: ");
		var int k = 0;
		for (String entry : importedPackages) {
			if(k != 0) {
				importStringBuilder.append(",");
			}
			importStringBuilder.append(entry);
			k++;
		}
		importStringBuilder.append("\n");
		
		var String importString = null;
		if(importStringBuilder.length  > 0) {
			importString = importStringBuilder.toString;
		}			 
			
		createManifest(projectName, requiredBundles, importedPackages, exportedPackages, monitor, project, importString);
	}
	
	def static createProject(IProgressMonitor progressMonitor, String name) {
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
	def static setupProject(IProgressMonitor progressMonitor, IProject compilerProject) {		

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
		
		// add entries which are already present in default JavaCore config
		entries.addAll(Arrays.asList(compilerJavaProject.getRawClasspath()));
		
		// add default JRE to classpath
		// TODO check if JRE is configured?
		entries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
		
		// add pde libraries which are automatically loaded once Manifest is parsed
		entries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
		
		compilerJavaProject.setRawClasspath(entries, progressMonitor);
					
		// set bin folder; create the bin folder if it does not exist beforehand
		val IFolder binFolder = compilerProject.getFolder("bin");
		if(!binFolder.exists)
			binFolder.create(false, true, null);
			
		compilerJavaProject.setOutputLocation(binFolder.getFullPath(), progressMonitor);			
	
		return compilerProject
	}
	
	def static createPluginXml(IProject project, IProgressMonitor progressMonitor) {
		val String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<?eclipse version=\"3.4\"?>\n"
			+ "<plugin>\n"
//			+  "<extension point=\"org.eclipse.ui.startup\">\n"
//			+      "<startup class=\"gen.Startup\">\n"
//			+      "</startup>\n"
//			+   "</extension>\n"
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
	
	private def static createBuildProps(IProgressMonitor progressMonitor, IProject project,
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

	private def static createManifest(String projectName, Set<String> requiredBundles,  Set<String> importedPackages,
			List<String> exportedPackages, IProgressMonitor progressMonitor, IProject project, String lastLine)
	throws CoreException {
		val StringBuilder maniContent = new StringBuilder("Manifest-Version: 1.0\n");
		maniContent.append("Comment: Do NOT MODIFY THIS FILE, use import-package in .karl file for dependencies\n")
//		maniContent.append("See-Second-Line-Heads-Up: \n");
//		maniContent.append("See-Second-Line-Caution: \n");
//		maniContent.append("See-Second-Line-Attention: \n");
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
		if(lastLine !== null) {
			// the last line of the manifest was supplied by the caller
			maniContent.append(lastLine);
		}
		
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
			RollbarExceptionReporting.INSTANCE.log(e, ErrorContext.PROJECT_BUILD, null);
				
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
	
	private def static IFile createFile(String name, IContainer container, String content,
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
			RollbarExceptionReporting.INSTANCE.log(e, ErrorContext.PROJECT_BUILD, null);
				
			e.printStackTrace
		}
		progressMonitor.worked(1);

		return file;
	}
	
	private def static void assertExist(IContainer c) {
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
	
	def static createActivator(IProject pluginProject, IProgressMonitor monitor, RuleFile ruleFile) {
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
        val Bundle bundle = FrameworkUtil.getBundle(KampRuleLanguageGenerator);
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
    
//    def static createServiceBase(IProject pluginProject, IProgressMonitor monitor) {
//    	val Bundle bundle = FrameworkUtil.getBundle(KampRuleLanguageGenerator);
//        var InputStream in = null;
//		try {
//			in = bundle.getEntry("resources/RuleProviderBase.java").openStream;        
//			pluginProject.getFolder("gen").getFile("RuleProviderBase.java").create(in, false, monitor)
//		} finally {
//			try { if(in !== null) in.close(); } finally {}
//		}
//    }
    
    def createLookupUtil(IProject pluginProject, IProgressMonitor monitor) {
    	val Bundle bundle = FrameworkUtil.getBundle(KampRuleLanguageGenerator);
        var InputStream in = null;
		try {
			in = bundle.getEntry("resources/LookupUtil.java").openStream;        
			pluginProject.getFolder("gen").getFile("LookupUtil.java").create(in, false, monitor)
		} finally {
			try { if(in !== null) in.close(); } finally {}
		}
    }
    
    def static createService(IProject pluginProject, IProgressMonitor monitor) {
        val Bundle bundle = FrameworkUtil.getBundle(KampRuleLanguageGenerator);
        var InputStream in = null;
		try {
			in = bundle.getEntry("resources/RuleProviderImpl.java").openStream;        
			pluginProject.getFolder("src").getFile("RuleProviderImpl.java").create(in, false, monitor)
		} finally {
			try { if(in !== null) in.close(); } finally {}
		}
    }
    
    def static removeGeneratedFolderContents(IProject destinationProject, IProgressMonitor monitor) {
		if(!destinationProject.getFolder("gen").exists)
			return;
			
		for(res : destinationProject.getFolder("gen").members) {
			res.delete(true, monitor)
		}
	}
	
	def static removeProject(IProject project, IProgressMonitor monitor) {
		project.delete(true, true, monitor);
	}
}