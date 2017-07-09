package edu.kit.ipd.sdq.kamp.ruledsl.generator

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.List
import java.util.stream.Collectors
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IFileSystemAccessExtension
import org.eclipse.xtext.generator.IFileSystemAccessExtension2

class PluginProjectFileSystemAccess implements IFileSystemAccess, IFileSystemAccessExtension, IFileSystemAccessExtension2 {
	protected List<IProject> projects
	protected List<IFolder> genFolders;
	protected IFileSystemAccess fsa;
	protected IFolder causingProjectFolder;

	new(List<IProject> project, IFileSystemAccess fsa, IFolder causingProjectFolder) {
		this.fsa = fsa;
		this.causingProjectFolder = causingProjectFolder;
		this.projects = project
		this.genFolders = project.stream.map([p | p.getFolder("gen")]).collect(Collectors.toList);
	}

	override void generateFile(String fileName, CharSequence contents) {
		fsa.generateFile(fileName, contents);
		this.genFolders.stream.forEach([f | f.getFile(fileName).create(new ByteArrayInputStream(contents.toString.getBytes(StandardCharsets.UTF_8)), true, new NullProgressMonitor)]);
	}

	override void generateFile(String fileName, String outputConfiguration, CharSequence contents) {
		// ignore the config
		generateFile(fileName, contents)
	}

	override void deleteFile(String fileName) {
		for(folder : this.genFolders) {
			val IFile file = folder.getFile(fileName);
			if(file.exists) {
				fsa.deleteFile(fileName)
				file.delete(true, new NullProgressMonitor)
			}
		}
	}

	override void deleteFile(String fileName, String ignoredOutputConfigurationName) {
		// ignore second parameter
		deleteFile(fileName)
	}

	override URI getURI(String fileName, String outputConfiguration) {
		// ignore output configuration
		getURI(fileName)
	}

	override URI getURI(String fileName) {
		val IFile file = this.causingProjectFolder.getFile(fileName);
		if(file.exists) {
			// from http://wiki.eclipse.org/EMF/FAQ#How_do_I_map_between_an_EMF_Resource_and_an_Eclipse_IFile.3F
			return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		}
		
		return null;
	}
}
