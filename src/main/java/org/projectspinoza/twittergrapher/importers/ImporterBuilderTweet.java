package org.projectspinoza.twittergrapher.importers;

import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileImporterBuilder.class)
public class ImporterBuilderTweet implements FileImporterBuilder {

	 public static final String IDENTIFER = "tweet";
	 
	 @Override
	 public FileImporter buildImporter() {
        return new ImporterTweet();
     }
	 
	 
	 @Override
     public String getName() {
        return IDENTIFER;
     }
	 
	 @Override
     public FileType[] getFileTypes() {
        //FileType ft = new FileType(".tweet", "tweets files");
        FileType ft = new FileType(".tweet", NbBundle.getMessage(getClass(), "fileType_TWEET_Name"));
        return new FileType[]{ft};
     }
	 
	 @Override
     public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("tweet");
     }


}
