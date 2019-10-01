/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebSearch;
import com.microstrategy.webapi.EnumDSSXMLFolderNames;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import com.microstrategy.webapi.EnumDSSXMLSearchDomain;
import com.microstrategy.webapi.EnumDSSXMLSearchFlags;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Anvesh
 */
public class HelperClass {
    
   public static HashMap<String, String> searchFolderAndGetId(String folderName, WebIServerSession serverSession) throws WebObjectsException {
        HashMap<String, String> folderIdAndName = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        WebFolder folder = null;
        WebObjectSource objSource = serverSession.getFactory().getObjectSource();
        try {
            WebSearch search = objSource.getNewSearchObject();
            search.setDisplayName("*");
            search.setSearchFlags(EnumDSSXMLSearchFlags.DssXmlSearchRootRecursive | EnumDSSXMLSearchFlags.DssXmlSearchNameWildCard);
            search.setNamePattern(folderName);
            search.setAsync(false);
            search.types().add(EnumDSSXMLObjectTypes.DssXmlTypeReportDefinition);
            search.types().add(EnumDSSXMLObjectTypes.DssXmlTypeDocumentDefinition);
            search.types().add(EnumDSSXMLObjectTypes.DssXmlTypeFolder);
            search.setDomain(EnumDSSXMLSearchDomain.DssXmlSearchDomainProject);
            String id = objSource.getFolderID(EnumDSSXMLFolderNames.DssXmlFolderNameRoot);
            search.setSearchRoot(id);

            search.submit();

            WebFolder f = search.getResults();

//            System.out.println("After search: " + f.size());
//            String folderId = "";
            WebObjectInfo objectInfo;
            if (f.size() > 0) {
                for (int i = 0; i < f.size(); i++) {
                    objectInfo = f.get(i);
                    if (folderName.equals(objectInfo.getDisplayName())) {
//                        folderId = objectInfo.getID();
                        if (objectInfo.getType() == 8) {
                            folderIdAndName.put(objectInfo.getID(), folderName);
                        }
//                        break;
                    }
                }
            }

            if ("".equals(folderIdAndName) || folderIdAndName == null) {
//                return "Folder Id " + folderId + " Does not Exist !";

            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            return ex.toString();
        }
        return folderIdAndName;
    }
    
}
