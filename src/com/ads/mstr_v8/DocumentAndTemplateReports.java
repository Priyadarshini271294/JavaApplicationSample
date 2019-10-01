/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Anvesh
 */

//Though every method code is same it is duplicated inorder to handle any issues in case if they exists in future
public class DocumentAndTemplateReports {

    public static List<HashMap<String, String>> getAllColumnsFromDocumentAssociatedWithReports(WebIServerSession serverSession, String objectName, String reportId, int objectType) throws WebObjectsException {
        List<HashMap<String, String>> columns = new ArrayList<>();
//        HashMap<String, String> eachColumn = new HashMap<>();
        WebObjectInfo info = MstrHelper.locateObjectWithName(serverSession, objectName, reportId);
        if (info == null) {
            return null;
        }
        
        columns=Mstr.getComponentsForObject(serverSession, info, columns, info.getDisplayName(), info.getID(), info.getType(),true,false);
        return columns;
    }
    
    
     public static List<HashMap<String, String>> getAllColumnsFromTemplate(WebIServerSession serverSession, String objectName, String reportId, int objectType) throws WebObjectsException {
        List<HashMap<String, String>> columns = new ArrayList<>();
//        HashMap<String, String> eachColumn = new HashMap<>();
        WebObjectInfo info = MstrHelper.locateObjectWithName(serverSession, objectName, reportId);
        if (info == null) {
            return null;
        }
        columns=Mstr.getComponentsForObject(serverSession, info, columns, info.getDisplayName(), info.getID(), info.getType(),false,false);
        return columns;
    }
     
     public static List<HashMap<String, String>> getAllColumnsFromReport(WebIServerSession serverSession, String objectName, String reportId, int objectType) throws WebObjectsException {
        List<HashMap<String, String>> columns = new ArrayList<>();
//        HashMap<String, String> eachColumn = new HashMap<>();
        WebObjectInfo info = MstrHelper.locateObjectWithName(serverSession, objectName, reportId);
        if (info == null) {
            return null;
        }
        columns=Mstr.getComponentsForObject(serverSession, info, columns, info.getDisplayName(), info.getID(), info.getType(),false,false);
        return columns;
    }
     
       public static List<HashMap<String, String>> getAllColumnsFromPrompt(WebIServerSession serverSession, String objectName, String reportId, int objectType) throws WebObjectsException {
        List<HashMap<String, String>> columns = new ArrayList<>();
//        HashMap<String, String> eachColumn = new HashMap<>();
        WebObjectInfo info = MstrHelper.locateObjectWithName(serverSession, objectName, reportId);
        if (info == null) {
            return null;
        }
        columns=Mstr.getComponentsForObject(serverSession, info, columns, info.getDisplayName(), info.getID(), info.getType(),false,true);
        return columns;
    }
    
}
