/*
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.ui;

import com.google.inject.Injector;
import edu.kit.ipd.sdq.kamp.ruledsl.ui.internal.RuledslActivator;
import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class KampRuleLanguageExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return RuledslActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return RuledslActivator.getInstance().getInjector(RuledslActivator.EDU_KIT_IPD_SDQ_KAMP_RULEDSL_KAMPRULELANGUAGE);
	}
	
}