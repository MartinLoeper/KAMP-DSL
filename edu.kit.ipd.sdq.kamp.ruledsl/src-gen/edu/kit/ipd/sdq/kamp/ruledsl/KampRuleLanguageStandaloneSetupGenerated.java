/*
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.kit.ipd.sdq.kamp.ruledsl.kampRuleLanguage.KampRuleLanguagePackage;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import tools.vitruv.dsls.mirbase.MirBaseStandaloneSetup;

@SuppressWarnings("all")
public class KampRuleLanguageStandaloneSetupGenerated implements ISetup {

	@Override
	public Injector createInjectorAndDoEMFRegistration() {
		MirBaseStandaloneSetup.doSetup();

		Injector injector = createInjector();
		register(injector);
		return injector;
	}
	
	public Injector createInjector() {
		return Guice.createInjector(new KampRuleLanguageRuntimeModule());
	}
	
	public void register(Injector injector) {
		if (!EPackage.Registry.INSTANCE.containsKey("http://www.kit.edu/ipd/sdq/kamp/ruledsl/KampRuleLanguage")) {
			EPackage.Registry.INSTANCE.put("http://www.kit.edu/ipd/sdq/kamp/ruledsl/KampRuleLanguage", KampRuleLanguagePackage.eINSTANCE);
		}
		IResourceFactory resourceFactory = injector.getInstance(IResourceFactory.class);
		IResourceServiceProvider serviceProvider = injector.getInstance(IResourceServiceProvider.class);
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("karl", resourceFactory);
		IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put("karl", serviceProvider);
	}
}
