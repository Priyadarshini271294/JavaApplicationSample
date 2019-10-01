/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

import com.microstrategy.web.objects.WebDisplayUnits;
import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.WebSearch;
import com.microstrategy.webapi.EnumDSSXMLFolderNames;
import com.microstrategy.webapi.EnumDSSXMLObjectFlags;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import com.microstrategy.webapi.EnumDSSXMLSearchDomain;
import com.microstrategy.webapi.EnumDSSXMLSearchFlags;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Anvesh
 */
public class MstrHelper {

    public static HashMap<String, String> browseSharedReportsFolder(String folderName, WebIServerSession serverSession) throws WebObjectsException {
        HashMap<String, String> reportIdAndName = new HashMap<>();
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
            HashSet<String> folderIds = new HashSet<String>();
//            String folderId = "";
            WebObjectInfo objectInfo;
            if (f.size() > 0) {
                for (int i = 0; i < f.size(); i++) {
                    objectInfo = f.get(i);
                    if (folderName.equals(objectInfo.getDisplayName())) {
//                        folderId = objectInfo.getID();
                        if (objectInfo.getType() == 8) {
                            folderIds.add(objectInfo.getID());
                        }
//                        break;
                    }
                }
            }

            if ("".equals(folderIds) || folderIds == null) {
//                return "Folder Id " + folderId + " Does not Exist !";

            }

            for (String folderId : folderIds) {
                folder = (WebFolder) objSource.getObject(folderId, 8);
                folder.setBlockBegin(1);
                folder.setBlockCount(50);
                folder.populate();
                if (folder.size() > 0) {
                    WebDisplayUnits units = folder.getChildUnits();
                    if (units != null) {
                        String name = "";
                        for (int i = 0; i < units.size(); i++) {
                            switch (units.get(i).getDisplayUnitType()) {
                                case 8:
//                                "Folder"
                                    serverSession.setApplicationType(8);
                                    reportIdAndName = getFolderReportObjects(objSource, units.get(i).getID(), serverSession, reportIdAndName);
                                    break;
                                case 32:
//                                "Project"

                                    break;
                                case 3:
//                                "Report"
                                    serverSession.setApplicationType(8);
                                    name = units.get(i).getDisplayName();
                                    reportIdAndName.put(units.get(i).getID(), name);
                                    break;
                                case 55:
//                                 "Document"
                                    reportIdAndName.put(units.get(i).getID(), units.get(i).getDisplayName());
                                    break;
                                case 1:
//                                "Filter"
                                    break;
                                case 2:
                                    reportIdAndName.put(units.get(i).getID(), "##template##" + units.get(i).getDisplayName());
//                                System.out.println("=="+units.get(i).getDisplayName());
//                                "Template"
                                    break;
                                case 18:
                                    // shortcut
                                    reportIdAndName.put(units.get(i).getID(), units.get(i).getDisplayName());
                                    break;
                                case 10:
                                    //prompt
//                                reportIdAndName.put(units.get(i).getID(), units.get(i).getDisplayName());
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            return ex.toString();
        }
        return reportIdAndName;
    }

   
    
    public static HashMap<String, String> getFolderReportObjects(WebObjectSource objSource, String folderId, WebIServerSession serverSession, HashMap<String, String> reportIdAndName) {
        WebFolder folder = null;
        StringBuilder sb = new StringBuilder();
        try {
            folder = (WebFolder) objSource.getObject(folderId, 8);
            folder.setBlockBegin(1);
            folder.setBlockCount(50);
            folder.populate();
            folder.setBlockBegin(1);
            folder.setBlockCount(50);
            folder.populate();
            if (folder.size() > 0) {
                WebDisplayUnits units = folder.getChildUnits();
                if (units != null) {
                    String query = "";
                    String name = "";
                    for (int i = 0; i < units.size(); i++) {
                        switch (units.get(i).getDisplayUnitType()) {
                            case 8:
//                                "Folder"
                                serverSession.setApplicationType(8);
                                reportIdAndName = getFolderReportObjects(objSource, units.get(i).getID(), serverSession, reportIdAndName);
                                break;
                            case 32:
//                                "Project"

                                break;
                            case 3:
//                                 "Report"
                                serverSession.setApplicationType(8);
                                name = units.get(i).getDisplayName();
                                reportIdAndName.put(units.get(i).getID(), name);
                                break;
                            case 55:
//                                 "Document"
                                name = units.get(i).getDisplayName();
                                reportIdAndName.put(units.get(i).getID(), name);
                                break;
                            case 1:
//                                 "Filter"

                                break;
                            case 2:
                                reportIdAndName.put(units.get(i).getID(), units.get(i).getDisplayName());
//                                 "Template"
                                break;
                            case 18:
                                //shortcut
                                reportIdAndName.put(units.get(i).getID(), units.get(i).getDisplayName());
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reportIdAndName;
    }

    

    public static WebObjectInfo locateObjectWithName(WebIServerSession serverSession, String objectName, String reportId) throws WebObjectsException {
        WebObjectsFactory woFact = serverSession.getFactory();
        WebObjectSource wos = woFact.getObjectSource();
        wos.setFlags(wos.getFlags() | EnumDSSXMLObjectFlags.DssXmlObjectComments);
        WebSearch search = wos.getNewSearchObject();
        //Set search parameters
        search.setSearchID(reportId);
        search.setNamePattern(objectName);
        // search.setSearchFlags(search.getSearchFlags() | EnumDSSXMLSearchFlags.DssXmlSearchNameWildCard);
        search.setAsync(false);
        search.setDomain(EnumDSSXMLSearchDomain.DssXmlSearchDomainProject);
        search.submit();

        WebFolder f = search.getResults();
        // System.out.println("After search: " + f.size());
        WebObjectInfo objectInfo = null;

//        System.out.println("Found " + f.size() + " objects");
        for (int i = 0; i < f.size(); i++) {
            if (f.size() > 0 && f.get(i).getID().equalsIgnoreCase(reportId)) {
                return f.get(i);
            }
        }

        return null;
    }
}
