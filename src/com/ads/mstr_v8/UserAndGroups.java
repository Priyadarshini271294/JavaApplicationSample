/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ads.mstr_v8;

import com.microstrategy.web.objects.WebAccessControlList;
import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSecurity;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.admin.users.WebPrivilegeEntry;
import com.microstrategy.web.objects.admin.users.WebPrivileges;
import com.microstrategy.web.objects.admin.users.WebUser;
import com.microstrategy.web.objects.admin.users.WebUserEntity;
import com.microstrategy.web.objects.admin.users.WebUserGroup;
import com.microstrategy.web.objects.admin.users.WebUserList;
import com.microstrategy.webapi.EnumDSSXMLObjectFlags;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anvesh
 */
public class UserAndGroups {

    public static WebObjectSource objectSource = null;

    public static HashMap<String, List<String>> groupWithUsers = new HashMap();

    public static HashMap<String, List<String>> userWithPriveleges = new HashMap();

    public static HashMap<String, List<String>> currentFolderAccessList = new HashMap();

    public static void clearStaticContent() {
        objectSource = null;
        groupWithUsers = new HashMap();
        userWithPriveleges = new HashMap();
        currentFolderAccessList = new HashMap();
    }

    public static HashMap<String, List<String>> getFolderDetails(WebIServerSession serverSession, String folderName, String everyOneGroupId, WebObjectSource webObjectSource) {
        // Setting up Session information
        clearStaticContent();
        objectSource = webObjectSource;
        try {
//            HashMap<String, String> folderIdsWithNames = HelperClass.browseSharedReportsFolder(folderName, serverSession);
            HashMap<String, String> folderIdsWithNames = HelperClass.searchFolderAndGetId(folderName, serverSession);
//            HashMap<String, String> folderIdsWithNames = new HashMap<>();
//            folderIdsWithNames.put("78AAF4654A62E9D384D0E094BE585507", "Customer Analysis");
            HashMap<String, String> folderAccessHolders = getFolderSecurityDetails(folderIdsWithNames, serverSession);
            //Id for group everyone
            WebUserList members = getListOfUsers(everyOneGroupId);
            Enumeration allUsers = members.elements();
            WebUser eachUser = null;
            while (allUsers.hasMoreElements()) {
                eachUser = (WebUser) allUsers.nextElement();
//                eachUser.getID();
//                eachUser.getDescription();
                    //List of privileges for each user
//                userWithPriveleges.put(eachUser.getName(), getAllPrivilegesForUser(eachUser));
                Enumeration userGroups = getGroupsAssocaitedWithUser(eachUser.getID());
                WebUserEntity eachGroup = null;
                while (userGroups.hasMoreElements()) {
                    eachGroup = (WebUserEntity) userGroups.nextElement();
                    if (groupWithUsers.get(eachGroup.getName()) == null) {
                        List users = new ArrayList<>();
                        users.add(eachUser.getName());
                        groupWithUsers.put(eachGroup.getName(), users);
                    } else {
                        groupWithUsers.get(eachGroup.getName()).add(eachUser.getName());
                    }
                }
                if (folderAccessHolders.get(eachUser.getID()) != null) {
                    //user
                    Enumeration userGroups2 = getGroupsAssocaitedWithUser(eachUser.getID());
                    WebUserEntity eachGroup2 = null;
                    while (userGroups2.hasMoreElements()) {
                        eachGroup2 = (WebUserEntity) userGroups2.nextElement();
                        String groupName = eachGroup2.getName();
                        List<String> user = new ArrayList();
                        user.add(eachUser.getName());
                        currentFolderAccessList.put(groupName, user);
                    }
                }

            }
            
            //for groups
            buildCompleteFolderAccessList(folderAccessHolders);
        } catch (WebObjectsException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return currentFolderAccessList;
    }

    //for groups
    public static void buildCompleteFolderAccessList(HashMap<String, String> folderAccessHolders) {
        for (Map.Entry<String, String> entry : folderAccessHolders.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue();
            if (name.startsWith("####")) {
                String groupName = name.split("####")[1].trim();
                currentFolderAccessList.put(groupName, groupWithUsers.get(groupName));
            }
        }
//        return currentFolderAccessList;
    }

    public static WebUserList getListOfUsers(String everyOneGroupId) {
        WebUserList webUserList = null;
        try {
            WebObjectInfo webObjectInfoForGroup = objectSource.getObject(everyOneGroupId, EnumDSSXMLObjectTypes.DssXmlTypeUser);
            webObjectInfoForGroup.populate();
            WebUserGroup group = (WebUserGroup) webObjectInfoForGroup;
            webUserList = group.getMembers();
        } catch (WebObjectsException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return webUserList;
    }

    public static Enumeration getGroupsAssocaitedWithUser(String userId) {
        Enumeration groupList = null;
        try {
            WebObjectInfo webObjectInfoForUser = objectSource.getObject(userId, EnumDSSXMLObjectTypes.DssXmlTypeUser);
            webObjectInfoForUser.populate();
            WebUser user = (WebUser) webObjectInfoForUser;
            WebUserList parentList = null;
            parentList = user.getParents();
            groupList = parentList.elements();
        } catch (WebObjectsException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return groupList;
    }

    public static List<String> getAllPrivilegesForUser(WebUser user) throws Exception {
        List<String> listOfPrivileges = new ArrayList();
        WebPrivileges webPrivileges = user.getAllPrivileges();
        for (int i = 0; i < webPrivileges.size(); i++) {
            WebPrivilegeEntry webPrivilegeEntry = webPrivileges.get(i);
            listOfPrivileges.add(webPrivilegeEntry.getName() + " #" + webPrivilegeEntry.getType());
//            webPrivilegeEntry.getDescription();
//            webPrivilegeEntry.getType();
        }
        return listOfPrivileges;
    }

    
    //returns folderName or GroupName with Ids
    public static HashMap<String, String> getFolderSecurityDetails(HashMap<String, String> folderIdsWithNames, WebIServerSession serverSession) {
        HashMap<String, String> folderAccessHolders = new HashMap();
        try {
            for (Map.Entry<String, String> entry : folderIdsWithNames.entrySet()) {
                String folderId = entry.getKey();
                String folderName = entry.getValue();
                WebObjectsFactory folderObjectsFactory = serverSession.getFactory();
                WebObjectSource folderObjectSource = folderObjectsFactory.getObjectSource();
                folderObjectSource.setFlags(folderObjectSource.getFlags() | EnumDSSXMLObjectFlags.DssXmlObjectTotalObject);
                WebObjectInfo folderObjectInfo = folderObjectSource.getObject(folderId, EnumDSSXMLObjectTypes.DssXmlTypeFolder);
                WebFolder folder = (WebFolder) folderObjectInfo;
                WebObjectSecurity woss = folderObjectInfo.getSecurity();
                WebAccessControlList acl = woss.getACL();
                for (int i = 0; i < acl.size(); i++) {
                    //gets the users/group under Security for this object
                    if (acl.get(i).getTrustee().isGroup()) {
                        folderAccessHolders.put(acl.get(i).getTrustee().getID(), "#### " + acl.get(i).getTrustee().getDisplayName());
//                        System.out.println("Trustee : " +acl.get(i).getTrustee().getDisplayName()+"(Group)");
                    } else {
                        folderAccessHolders.put(acl.get(i).getTrustee().getID(), acl.get(i).getTrustee().getDisplayName());
//                        System.out.println("Trustee : " + acl.get(i).getTrustee().getDisplayName()+"(User)");

                    }

                    //get the rights for each user/group
//                    System.out.println("Rights :" + acl.get(i).getRights());
                    //change acl to modify for all group/users
//                    acl.get(i).setRights(223);
//                    acl.get(i).setRights(EnumDSSXMLAccessRightFlags.DssXmlAccessRightBrowse);
//                    wos2.save(woi);
                }

            }
        } catch (WebObjectsException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return folderAccessHolders;
    }

}
