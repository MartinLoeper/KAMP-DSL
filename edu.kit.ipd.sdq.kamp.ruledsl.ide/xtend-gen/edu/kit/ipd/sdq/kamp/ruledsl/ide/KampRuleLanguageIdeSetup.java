/**
 * generated by Xtext 2.11.0
 */
package edu.kit.ipd.sdq.kamp.ruledsl.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.kit.ipd.sdq.kamp.ruledsl.KampRuleLanguageRuntimeModule;
import edu.kit.ipd.sdq.kamp.ruledsl.KampRuleLanguageStandaloneSetup;
import edu.kit.ipd.sdq.kamp.ruledsl.ide.KampRuleLanguageIdeModule;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
@SuppressWarnings("all")
public class KampRuleLanguageIdeSetup extends KampRuleLanguageStandaloneSetup {
  @Override
  public Injector createInjector() {
    KampRuleLanguageRuntimeModule _kampRuleLanguageRuntimeModule = new KampRuleLanguageRuntimeModule();
    KampRuleLanguageIdeModule _kampRuleLanguageIdeModule = new KampRuleLanguageIdeModule();
    return Guice.createInjector(Modules2.mixin(_kampRuleLanguageRuntimeModule, _kampRuleLanguageIdeModule));
  }
}