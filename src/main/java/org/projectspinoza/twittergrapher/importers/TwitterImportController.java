package org.projectspinoza.twittergrapher.importers;

import java.io.File;
import java.io.FileNotFoundException;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.impl.ImportControllerImpl;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

//@ServiceProvider(service = ImportController.class)
public class TwitterImportController extends ImportControllerImpl {
	private static TwitterImportController twitterImportController = null;
	
	private TwitterImportController(){
		super();
	}
	
	@Override
    public Container importFile(File file) throws FileNotFoundException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            //fileObject = getArchivedFile(fileObject);   //Unzip and return content file
            //FileImporterBuilder builder = getMatchingImporter(fileObject);
        	FileImporterBuilder builder = new ImporterBuilderTweet();
            if (fileObject != null && builder != null) {
                Container c = importFile(fileObject.getInputStream(), builder.buildImporter());
                return c;
            }
        }
        return null;
    }
    
    public static synchronized TwitterImportController getInstance(){
    	if(twitterImportController == null){
    		twitterImportController = new TwitterImportController();
    	}
    	
    	return twitterImportController;
    }
}
