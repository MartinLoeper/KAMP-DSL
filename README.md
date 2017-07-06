# KAMP-DSL
This project contains the Domain Specific Language for the KAMP Framework.
It was created in the course of student assistant activity at the Karlsruhe Institute of Technology / IPD.

## Install
- Download and Install [Eclipse IDE for Java Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygenr)
- Change file encoding to UTF-8: Window -> Preferences -> General -> Workspace : Text file encoding
- Download and Install as Eclipse Extension: [Eclipse Modeling Framework](http://www.eclipse.org/downloads/packages/eclipse-modeling-tools/neon3)
- Install [Palladio as Eclipse Extension](https://sdqweb.ipd.kit.edu/eclipse/palladiosimulator/nightly/) - You can alternatively check out the sources into the workbench from: https://github.com/PalladioSimulator
- Import the [KAMP-Framework and Submodule Projects](https://github.com/KAMP-Research/KAMP)  into Workbench
- Import [SDQ Commons](https://github.com/kit-sdq/SDQ-Commons) into Workbench
- Import [Vitruv](https://github.com/vitruv-tools/Vitruv) into Workbench
- Download and Install as Eclipse Extension: [Xtext](https://eclipse.org/Xtext/download.html)
- Download and Install as Eclipse Extension: [Xtend](https://eclipse.org/Xtext/download.html)
- Create working sets for each imported repository

## Configure
- Navigate into the project *edu.kit.ipd.sdq.kamp.ruledsl* and into *src/edu.kit.ipd.sdq.kamp.ruledsl*. Right click on *GenerateKampRuleLanguage.mwe2* and Run As -> MWE2 Workflow
- Run KAMP as Eclipse Application
- Ignore minor errors in Vitruv and related projects
- Create a file with .karl extensions inside a KAMP4BP project
- You have to apply quick fixed proposed by the Editor
- If you get an invalid Manifest Header Exception in the inner Eclipse, navigate into the inner Eclipse Workspace and into your Project -> META-INF -> MANIFEST.MF and remove trailing commas for *Require-Bundle:* attribute
