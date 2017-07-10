# KAMP-DSL
This project contains the Domain Specific Language for the KAMP Framework.
It was created in the course of student assistant activity at the Karlsruhe Institute of Technology / IPD.

The old SVN Repository URL is: https://svnserver.informatik.kit.edu/i43/svn/code/Palladio/Addons/KAMPIntBIISRequirements/branches/generalized-KAMP-rule-generalization/KAMPRuleLanguage/

## Install
- Download and Install [Eclipse IDE for Java Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygenr)
- Change file encoding to UTF-8: Window -> Preferences -> General -> Workspace : Text file encoding
- Download and Install as Eclipse Extension: [Eclipse Modeling Framework](http://www.eclipse.org/downloads/packages/eclipse-modeling-tools/neon3)
- Install [Palladio as Eclipse Extension](https://sdqweb.ipd.kit.edu/eclipse/palladiosimulator/nightly/) - You can alternatively check out the sources into the workbench from: ~~https://github.com/PalladioSimulator~~ see optional import below
- Import the [KAMP-Framework and Submodule Projects](https://github.com/KAMP-Research/KAMP)  into Workbench
- Import [SDQ Commons](https://github.com/kit-sdq/SDQ-Commons) into Workbench
- Import [Vitruv](https://github.com/vitruv-tools/Vitruv) into Workbench
- Import [Palladio](https://svnserver.informatik.kit.edu/i43/svn/code/Palladio/Core/trunk/PCM/) into Workbench as SVN Project (optional) - this will enable you access to the pcm.ecore model
- Download and Install as Eclipse Extension: [GEF](https://projects.eclipse.org/projects/tools.gef/downloads) and install the following via marketplace: *EcoreTools: Ecore Diagram Editor* (both optional) - this is necessary if you included the Palladio sources in the step above and want to create a graphical model
- Download and Install as Eclipse Extension: [Xtext](https://eclipse.org/Xtext/download.html)
- Download and Install as Eclipse Extension: [Xtend](https://eclipse.org/Xtext/download.html)
- Create working sets for each imported repository

## Configure
- Navigate into the project *edu.kit.ipd.sdq.kamp.ruledsl* and into *src/edu.kit.ipd.sdq.kamp.ruledsl*. Right click on *GenerateKampRuleLanguage.mwe2* and Run As -> MWE2 Workflow
- Run KAMP as Eclipse Application
- Ignore minor errors in Vitruv and related projects
- Create a file with .karl extensions inside a KAMP4BP project
- You have to apply quick fixes proposed by the Editor
- If you get an invalid Manifest Header Exception in the inner Eclipse, navigate into the inner Eclipse Workspace and into your Project -> META-INF -> MANIFEST.MF and remove trailing commas for *Require-Bundle:* attribute

## DSL Editor Requirements
- the KAMP project needs the Java nature and JRE on classpath
- possibly even a JDK  is necessary as we are using JDT to compile the Eclipse Plugin Project
- the MANIFEST.mf file must import all necessary models which are referenced from .karl file via import (QuickFix available) as required bundles
- the MANIFEST.mf file must import all Java classes which are referenced by KampRuleLanguageJvmModelInferrer as required bundles

## Wiki
For more information please navigate into the Wiki of this project.
