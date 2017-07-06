/*
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.ide

import com.google.inject.Guice
import edu.kit.ipd.sdq.kamp.ruledsl.KampRuleLanguageRuntimeModule
import edu.kit.ipd.sdq.kamp.ruledsl.KampRuleLanguageStandaloneSetup
import org.eclipse.xtext.util.Modules2

/**
 * Initialization support for running Xtext languages as language servers.
 */
class KampRuleLanguageIdeSetup extends KampRuleLanguageStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new KampRuleLanguageRuntimeModule, new KampRuleLanguageIdeModule))
	}
	
}
