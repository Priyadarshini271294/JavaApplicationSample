/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anvesh
 */
public class CallerClass {

    public static void main(String[] args) {

        String username = "Administrator";
        String password = "";
        String server = "25.45.184.18";
        String projName = "MicroStrategy Tutorial";
        String ignoredColumnNames = "/,-,+,<,>,<=,>=,Aggregation,Average,Count,Geometric Mean,Max,Maximum,Median,Minimum,Mode,Standard Deviation,VerticalBar (9.3.2),VerticalBar,VerticalLine,Variance,US Regions & Web,And,In,Bubble,HorizontalBar,Gauge,Pie";
//        String ignoredColumnNames = "";
        String generateTemplatesAsReports = "yes";
        String folderName = "Customer Analysis";
//        String folderName = "Prompts";
//        String folderName = "Daily Analysis";
//        String folderName = "Visualizations";
//        String folderName = "Thresholds";
        String everyOneGroupId = "C82C6B1011D2894CC0009D9F29718E4F";
        HashMap<String, HashMap> globalMap = Mstr.startExecution(username, password, server, projName, folderName, generateTemplatesAsReports, ignoredColumnNames, everyOneGroupId);
        if (globalMap.get("Exception") != null) {
            System.out.println(globalMap.get("Exception").get("exception"));
        } else {
            HashMap<String, List<String>> accessPermission = globalMap.get("folderSecurity");
            HashMap<String, HashMap<String, String>> reportMap = globalMap.get("reportMap");
            HashMap<String, List<HashMap<String, String>>> reportColumns = globalMap.get("reportColumns");
            for (Map.Entry<String, List< String>> entry : accessPermission.entrySet()) {
                String groupName = entry.getKey();
                List<String> users = entry.getValue();
                System.out.println("Group Name : " + groupName);
                for (String user : users) {
                    System.out.println("User : " + user);
                }
            }
            Iterator itr = reportMap.entrySet().iterator();
            System.out.println("************************************************************");
            while (itr.hasNext()) {
                Map.Entry pair = (Map.Entry) itr.next();
                String reportName = (String) pair.getKey();
                HashMap<String, String> reportProp = (HashMap<String, String>) pair.getValue();
                System.out.println("ReportName : " + reportName);
                System.out.println("Id : " + reportProp.get("Id"));
                System.out.println("Description : " + reportProp.get("Description"));
                System.out.println("Long Description : " + reportProp.get("Long Description"));
                System.out.println("Object Type : " + reportProp.get("Object Type"));
                System.out.println("Object SubType : " + reportProp.get("Object SubType"));
                System.out.println("Hidden : " + reportProp.get("Hidden"));
                List<HashMap<String, String>> reportCols = reportColumns.get(reportName);
                for (int i = 0; i < reportCols.size(); i++) {
                    HashMap<String, String> eachCol = reportCols.get(i);
                    System.out.println("Column Name: " + eachCol.get("column"));
                    System.out.println("Description: " + eachCol.get("Description"));
                    System.out.println("Long Description: " + eachCol.get("Long Description"));
                    System.out.println("Id: " + eachCol.get("Id"));
                    System.out.println("Object Type: " + eachCol.get("Object Type"));
                    System.out.println("Object SubType: " + eachCol.get("Object SubType"));
                    System.out.println("Hidden: " + eachCol.get("Hidden"));
                }
                System.out.println("--------------------------------------");
            }
        }
    }

}
