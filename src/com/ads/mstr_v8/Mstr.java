/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

/**
 *
 * @author Sadar
 */
import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.WebSearch;
import com.microstrategy.webapi.EnumDSSXMLPropertyXmlFlags;
import com.microstrategy.webapi.EnumDSSXMLSearchDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Mstr {

    public static void main(String[] args) {
        String username = "Administrator";
        String password = "";

        String server = "10.1.50.56";
        String projName = "ErwinFirstProject";
        String folderName = "";
        String generateTemplatesAsReports = "";
        String ignoredColumns = "";
        String everyOneGroupId = "";
        startExecution(username,
                password, server,
                projName, folderName,
                generateTemplatesAsReports, ignoredColumns,
                everyOneGroupId
        );
    }
    public static HashMap<String, HashMap<String, String>> reportProperties = new HashMap<>();

    public static HashMap<String, List<HashMap<String, String>>> reportColumns = new HashMap<>();

    public static HashSet<String> ignoredColumnNames = new HashSet<String>();

    public static WebObjectsFactory factory = null;

    public static WebObjectSource objectSource = null;

    public static void clearStaticConetent() {
        reportColumns = new HashMap<>();
        reportProperties = new HashMap<>();
        ignoredColumnNames = new HashSet<>();
        factory = null;
        objectSource = null;
    }

    public static HashSet<String> igonoredColumnNames(String ignoredColumns) {
        HashSet<String> ignoredColumnNames = new HashSet<String>();
        for (String eachColumn : ignoredColumns.split(",")) {
            ignoredColumnNames.add(eachColumn);
        }
        return ignoredColumnNames;
    }

    public static HashMap<String, HashMap> startExecution(String username, String password, String server, String projName, String folderName, String generateTemplatesAsReports, String ignoredColumns, String everyOneGroupId) {
        clearStaticConetent();
        ignoredColumnNames = igonoredColumnNames(ignoredColumns);
        HashMap<String, HashMap> globalMap = new HashMap<>();
        try {
            WebIServerSession adminSession = createSession(username, password, server, projName);
            HashMap<String, String> reports = MstrHelper.browseSharedReportsFolder(folderName, adminSession);
            objectSource = factory.getObjectSource();
            HashMap<String, List<String>> currentFolderAccessList = UserAndGroups.getFolderDetails(adminSession, folderName, everyOneGroupId, objectSource);
            globalMap.put("folderSecurity", currentFolderAccessList);
            for (Map.Entry<String, String> eachReport : reports.entrySet()) {
                String reportId = eachReport.getKey();
                String reportName = eachReport.getValue();
                WebObjectInfo info = null;
                if (reportName.startsWith("##template##")) {
                    reportName = reportName.split("##template##")[1];
                    if (generateTemplatesAsReports.equalsIgnoreCase("yes")) {
                        info = MstrHelper.locateObjectWithName(adminSession, reportName, reportId);
                    } else {
                        continue;
                    }
                } else {
                    info = MstrHelper.locateObjectWithName(adminSession, reportName, reportId);
                }

                if (info == null) {
//                    System.out.println("No object returned, check the object name passed and try again\n");
                    return null;
                }
//                 WebObjectInfo woi=wos.getObject(f.get(0).getID(), EnumDSSXMLObjectTypes.DssXmlTypeUser, true);
                HashMap<String, String> reportProp = new HashMap<>();
//                  info.getChildUnits();

//                System.out.println("Report Name: " + info.getDisplayName() + "\nDescription :" + info.getDescription() + "\nId: " + info.getID());
                reportProp.put("Id", info.getID());
//                info.getOwner().getName(); 
                reportProp.put("Description", info.getDescription());
                reportProp.put("Object Type", info.getType() + "");
                reportProp.put("Object SubType", info.getSubType() + "");
                reportProp.put("Hidden", info.isHidden() + "");
                WebObjectInfo infoForComments = adminSession.getFactory().getObjectSource().getObject(info.getID(), info.getType());
                String cmnts[] = infoForComments.getComments();
                int count = 0;
                while (cmnts != null && count < cmnts.length) {
//                    System.out.println(info.getDisplayName() + " Report==> " + cmnts[count]);
                    reportProp.put("Long Description", cmnts[count]);
                    count++;
                }
                List<HashMap<String, String>> columns = new ArrayList<>();
                boolean isDocument = false;
                boolean isPrompt = false;
                columns = getComponentsForObject(adminSession, info, columns, reportName, reportId, info.getType(), isDocument, isPrompt);
//                getDependentsForObject(adminSession, info);
                reportColumns.put(info.getDisplayName(), columns);
                reportProperties.put(info.getDisplayName(), reportProp);
                globalMap.put("reportMap", reportProperties);
                globalMap.put("reportColumns", reportColumns);
//                System.out.println("*******End of Report " + reportName + "********");
            }

        } catch (WebObjectsException e) {
            e.printStackTrace();
            HashMap<String, String> excepion = new HashMap();
            excepion.put("exception", e.getMessage());
            globalMap.put("Exception", excepion);
            return globalMap;
        }
        return globalMap;
    }

    public static WebFolder getDependentsForObject(WebIServerSession serverSession, WebObjectInfo info) throws WebObjectsException {
//        System.out.println("--------------------Dependents----------------------");
        WebObjectsFactory woFact = serverSession.getFactory();
        WebObjectSource wos = woFact.getObjectSource();
        WebSearch search = wos.getNewSearchObject();

        //Set search parameters
        //Using wildcard * to return all
        search.setDisplayName("*");

        search.setAsync(false);
        search.setDomain(EnumDSSXMLSearchDomain.DssXmlSearchDomainProject);

        //Add the object we want to find components of to usedBy collection
        search.uses().add(info);

        //search.usedBy().add(info);
        search.submit();

        WebFolder f = search.getResults();

//        System.out.println("After search: " + f.size());
        WebObjectInfo objectInfo = null;

        if (f.size() > 0) {
            for (int i = 0; i < f.size(); i++) {
                objectInfo = f.get(i);
//                System.out.println("Type: " + objectInfo.getType() + " SubType: " + objectInfo.getSubType() + " " + objectInfo.getDisplayName());
            }
        } else {
//            System.out.println("No dependents found");
        }
        return f;
    }

    public static List<HashMap<String, String>> getComponentsForObject(WebIServerSession serverSession, WebObjectInfo info, List<HashMap<String, String>> columns,
            String reportName, String reportId, int objectType, boolean isDocument, boolean isPrompt) throws WebObjectsException {
//        System.out.println("--------------------Components----------------------");

        WebObjectsFactory woFact = serverSession.getFactory();
        WebObjectSource wos = woFact.getObjectSource();

        WebSearch search = wos.getNewSearchObject();
        search.setSearchFlags(1);
//        search.setSearchFlags(search.getSearchFlags() + EnumDSSXMLSearchFlags.DssXmlSearchUsedByRecursive);
        //Set search parameters
        //Using wildcard * to return all
//        search.setDisplayName("*");
        search.setAsync(false);
        search.setFlags(search.getFlags() | ~EnumDSSXMLPropertyXmlFlags.DssPropertyXmlPropAll);
        search.setDomain(EnumDSSXMLSearchDomain.DssXmlSearchDomainProject);
        //Add the object we want to find components of to usedBy collection
        search.usedBy().add(info);
        search.submit();

        WebFolder f = search.getResults();
//        EnumDSSXMLObjectSubTypes
//        System.out.println("After search: " + f.size());
        WebObjectInfo objectInfo = null;
        if (f.size() > 0) {
            for (int i = 0; i < f.size(); i++) {
                objectInfo = f.get(i);

                if (objectType == 55) {
                    // if the report is document type
                    if (objectInfo.getType() == 3) {
                        // if the document has reports the to get report columns this condition is placed
                        List<HashMap<String, String>> reportColumns = DocumentAndTemplateReports.getAllColumnsFromDocumentAssociatedWithReports(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectType);
                        if (reportColumns != null) {
                            columns.addAll(reportColumns);
                        }
                    } else {
                        //other than reports (report columns/attribute/metrics) there are few functions generated in document that is why this condition has been written
                        // wrote this thinking it will give f(x) but its not giving 
                        HashMap<String, String> eachColumn = addColumn(reportName, objectInfo, wos, true, false);
                        if (eachColumn != null) {
                            columns.add(eachColumn);
                        }

                    }
                } else if (objectType == 18) {
                    //for shortcut (18) : shortcut objects may be of type report,document and template
                    if (objectInfo.getType() == 55) {
                        // if the shortcut is document which  has reports the to get report columns this condition is placed
                        List<HashMap<String, String>> reportColumns = DocumentAndTemplateReports.getAllColumnsFromDocumentAssociatedWithReports(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectType);
                        if (reportColumns != null) {
                            columns.addAll(reportColumns);
                        }
                    } else if (objectInfo.getType() == 2) {
//                        if the shortcut is template then to get tempate columns in this shortcut this condition is placed
                        List<HashMap<String, String>> templateColumns = DocumentAndTemplateReports.getAllColumnsFromTemplate(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectInfo.getType());
                        if (templateColumns != null) {
                            columns.addAll(templateColumns);
                        }
                    } else {
                        //if the shortcut is of type report to get report columns
                        List<HashMap<String, String>> reportColumns = DocumentAndTemplateReports.getAllColumnsFromReport(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectInfo.getType());
                        if (reportColumns != null) {
                            columns.addAll(reportColumns);
                        }
                    }
                } else {
                    if (objectInfo.getType() == 2) {
//                        if the report has template then to get tempate columns in this report this condition is placed
                        List<HashMap<String, String>> templateColumns = DocumentAndTemplateReports.getAllColumnsFromTemplate(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectInfo.getType());
                        if (templateColumns != null) {
                            columns.addAll(templateColumns);
                        }
                    } else if (objectInfo.getType() == 10) {
                        // for reports having prompts to get their columns
                        List<HashMap<String, String>> promptColumns = DocumentAndTemplateReports.getAllColumnsFromPrompt(serverSession, objectInfo.getDisplayName(), objectInfo.getID(), objectInfo.getType());
                        if (promptColumns != null) {
                            columns.addAll(promptColumns);
                        }
                    } else {
                        HashMap<String, String> eachColumn = addColumn(reportName, objectInfo, wos, isDocument, isPrompt);
                        if (eachColumn != null) {
                            columns.add(eachColumn);
                        }
                    }
                }

            }
        } else {
//            System.out.println("No dependents found");
        }

        return columns;
    }

    public static HashMap<String, String> addColumn(String reportName, WebObjectInfo objectInfo, WebObjectSource wos, boolean isDocument, boolean isPrompt) {
        HashMap<String, String> eachColumn = new HashMap();
        try {
            if (ignoredColumnNames.contains(objectInfo.getName())) {
                return null;
            }
            if (isDocument == true) {
                // to differentiate particular column is coming from which report
//                eachColumn.put("column", reportName + "==>" + objectInfo.getName());
                eachColumn.put("column", objectInfo.getName());

            } else if (isPrompt == true) {
//                eachColumn.put("column", reportName + "==>" + objectInfo.getName());
                eachColumn.put("column", objectInfo.getName());
            } else {
                eachColumn.put("column", objectInfo.getName());
            }
            eachColumn.put("Description", objectInfo.getDescription());
            eachColumn.put("Id", objectInfo.getID());
            eachColumn.put("Object Type", objectInfo.getType() + "");
            eachColumn.put("Object SubType", objectInfo.getSubType() + "");
            eachColumn.put("Hidden", objectInfo.isHidden() + "");

            WebObjectInfo objectInfoForComments = wos.getObject(objectInfo.getID(), objectInfo.getType(), true);
            String cmnts[] = objectInfoForComments.getComments();
            int count = 0;
            while (cmnts != null && count < cmnts.length) {
//                        System.out.println(objectInfo.getDisplayName()+"Column==> " + cmnts[count]);
                eachColumn.put("Long Description", cmnts[count]);
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return eachColumn;
    }

    public static WebIServerSession createSession(String userName, String password, String serverIp, String projName) throws WebObjectsException {
        //Create session
        WebIServerSession serverSession = null;
        try {
            factory = WebObjectsFactory.getInstance();
            objectSource = factory.getObjectSource();
            serverSession = factory.getIServerSession();
            serverSession.setServerName(serverIp);
            serverSession.setLogin(userName);
            serverSession.setPassword(password);
            serverSession.setProjectName(projName);
            serverSession.getSessionID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverSession;

        //Create session 
    }

}
