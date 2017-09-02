package edu.kit.ipd.sdq.kamp.ruledsl.generator

import com.google.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IFileSystemAccessExtension2
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator

class KampRuleLanguageGenerator implements IGenerator {
	
	@Inject JvmModelGenerator jvmModelGenerator;
	
	// if set true, project does not get delete if an error occurs
	public static final boolean DEBUG = true;
	
	override doGenerate(Resource resource, IFileSystemAccess fsa) {		
		synchronized(KampRuleLanguageGenerator) {
			// delegate to Java generator in order to generate rule class files
			jvmModelGenerator.doGenerate(resource, fsa)
						
	    	val config = new Configuration(resource, fsa as IFileSystemAccessExtension2);
	    	var KarlJobBase job = if(config.isKarlProjectExisting) new KarlProjectUpdateJob(config) else new KarlProjectCreationJob(config);
		
			// reserve exclusive write access to the project... should be done but we get sync issues here
			// job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			
			job.trySchedule();
    	}
	}
}