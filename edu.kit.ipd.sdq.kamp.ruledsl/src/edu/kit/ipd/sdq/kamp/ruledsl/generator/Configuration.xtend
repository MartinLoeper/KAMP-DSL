package edu.kit.ipd.sdq.kamp.ruledsl.generator

import java.util.List
import org.eclipse.xtext.common.types.JvmDeclaredType
import java.util.ArrayList
import org.eclipse.xtext.generator.IFileSystemAccessExtension2
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.RuleFile
import org.eclipse.emf.ecore.EPackage

import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.*
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.util.URI
import java.util.Set

class Configuration {
	private final String assignedProjectName;
	private final Resource resource;
	private final Set<String> packageUris = newHashSet;
	private final List<URI> jFileUris = newArrayList();
    private final List<String> jFileNames = newArrayList();
	private final RuleFile ruleFile;	// the root rule file
	
	new(Resource resource, IFileSystemAccessExtension2 fileSystemAccess) {
		this.resource = resource;
       	var List<JvmDeclaredType> decTypes = new ArrayList();

       	// get all classes which were created
       	// each class is a single rule
       	for (obj : resource.contents) {
			if(obj instanceof JvmDeclaredType) {
				decTypes.add(obj)
			}
		}
       	
       	// obtain the project name from file system access
    	// FIXME this is actually a very unstable implementation. Look for a better way to find the project where the karl file is located
	    // FIXME this breaks the eclipse 'linked' project implementation - projects must be imported into the workspace because of this!!
	    val path = root.getFile(new Path(fileSystemAccess.getURI("").toPlatformString(true)))
	    this.assignedProjectName = path.parent.name;
       	
       	// copy the generated files to project folder
    	for(res : decTypes) {
    		if(res instanceof JvmDeclaredType) {
    			val cFileName = res.simpleName.replace('.', '/') + '.java';        			
	        	jFileNames.add(cFileName);
	        	
	        	val cUri = fileSystemAccess.getURI("gen/rule/" + cFileName);
	        	jFileUris.add(cUri);
	        	
	        	println("Generated file is located under: " + cUri);
	        }
    	}       
        
        // get metamodel import statements
		val ruleFiles = resource.contents.filter[elist | elist instanceof RuleFile]		
		if(ruleFiles.empty) {
			// TODO handle properly - we do not want crashes if .karl file is empty?!
			throw new IllegalStateException("No RuleFile present in input file.");
		}
		
		this.ruleFile = ruleFiles.head as RuleFile

		for(metamodellImport : ruleFile.metamodelImports) {
			val package = metamodellImport.package as EPackage
			packageUris.add(package.nsURI)
		}
	}
	
	def String getAssignedProjectName() {
		return assignedProjectName;
	}
	
	def boolean isKarlProjectExisting() {
		return root.getProject(assignedProjectName + "-rules").exists;
	}
	
	def getResource() {
		return resource
	}
	
	def getPackageUris() {
		return packageUris
	}
	
	def getSourceFileUris() {
		return jFileUris
	}
	
	def getSourceFileNames() {
		return jFileNames;
	}
	
	def getRootRuleFile() {
		return ruleFile
	}
}
