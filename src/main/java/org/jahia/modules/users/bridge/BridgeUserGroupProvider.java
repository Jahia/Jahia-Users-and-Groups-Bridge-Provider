/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.users.bridge;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import org.jahia.modules.external.users.GroupNotFoundException;
import org.jahia.modules.external.users.Member;
import org.jahia.modules.external.users.UserGroupProvider;
import org.jahia.modules.external.users.UserNotFoundException;
import org.jahia.services.usermanager.*;

import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import java.security.Principal;
import java.util.*;

/**
 * A provider implementation for the bridge
 * @author kevan
 */
public class BridgeUserGroupProvider implements UserGroupProvider {
    private String providerKey;
    private JahiaUserManagerProvider userManagerProvider;
    private JahiaGroupManagerProvider groupManagerProvider;

    public BridgeUserGroupProvider(String providerKey) {
        this.providerKey = providerKey;
    }

    @Override
    public JahiaUser getUser(String name) throws UserNotFoundException {
        if(isUserManagerAvailable()){
            JahiaUser user = userManagerProvider.lookupUser(name);
            if(user != null) {
                return user;
            } else {
                throw new UserNotFoundException("unable to find user " + name + " on provider " + providerKey);
            }
        }
        return null;
    }

    @Override
    public JahiaGroup getGroup(String name) throws GroupNotFoundException {
        if(isGroupManagerAvailable()){
            JahiaGroup group = groupManagerProvider.lookupGroup(0, name);
            if(group != null) {
                return group;
            } else {
                throw new GroupNotFoundException("unable to find group " + name + " on provider " + providerKey);
            }
        }
        return null;
    }

    @Override
    public List<Member> getGroupMembers(String groupName) {
        if(isGroupManagerAvailable()){
            JahiaGroup jahiaGroup = groupManagerProvider.lookupGroup(groupName);
            List<Member> members = new ArrayList<Member>();
            if(jahiaGroup != null){
                Collection<Principal> principals = jahiaGroup.getMembers();
                for (Principal principal : principals){
                    if(principal instanceof java.security.acl.Group) {
                        members.add(new Member(principal.getName(), Member.MemberType.GROUP));
                    } else {
                        members.add(new Member(principal.getName(), Member.MemberType.USER));
                    }
                }
                return members;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMembership(Member member) {
        if(isUserManagerAvailable() && member.getType().equals(Member.MemberType.USER)){
            JahiaUser user = userManagerProvider.lookupUser(member.getName());
            if(user != null && isGroupManagerAvailable()){
                List<String> keys = groupManagerProvider.getUserMembership(user);
                return new ArrayList<String>(Collections2.filter(Collections2.transform(keys, new Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable String input) {
                        if (input != null) {
                            JahiaGroup group = groupManagerProvider.lookupGroup(input);
                            if(group != null){
                                return group.getName();
                            }
                        }
                        return null;
                    }
                }), Predicates.notNull()));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> searchUsers(Properties searchCriterias, long offset, long limit) {
        List<String> users = new ArrayList<String>();
        if(isUserManagerAvailable()){
            Set<JahiaUser> jahiaUsers = userManagerProvider.searchUsers(searchCriterias);
            for (JahiaUser jahiaUser : jahiaUsers) {
                users.add(jahiaUser.getName());
            }
        }
        return users.subList(Math.min((int) offset, users.size()), limit < 0 ? users.size() : Math.min((int) (offset + limit), users.size()));
    }

    @Override
    public List<String> searchGroups(Properties searchCriterias, long offset, long limit) {
        List<String> groups = new ArrayList<String>();
        if(isGroupManagerAvailable()){
            Set<JahiaGroup> jahiaGroups = groupManagerProvider.searchGroups(0, searchCriterias);
            for (JahiaGroup jahiaGroup : jahiaGroups) {
                groups.add(jahiaGroup.getGroupname());
            }
        }
        return groups.subList(Math.min((int) offset, groups.size()), limit < 0 ? groups.size() : Math.min((int) (offset + limit), groups.size()));
    }

    @Override
    public boolean verifyPassword(String userName, String userPassword) {
        if(isUserManagerAvailable()){
            JahiaUser user = userManagerProvider.lookupUser(userName);
            if(user != null) {
                return userManagerProvider.login(user.getUserKey(), userPassword);
            }
        }
        return false;
    }

    @Override
    public boolean supportsGroups() {
        return true;
    }

    @Override
    public boolean isAvailable() throws RepositoryException {
        return true;
    }

    private boolean isGroupManagerAvailable(){
        return groupManagerProvider != null;
    }

    private boolean isUserManagerAvailable(){
        return userManagerProvider != null;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public JahiaUserManagerProvider getUserManagerProvider() {
        return userManagerProvider;
    }

    public void setUserManagerProvider(JahiaUserManagerProvider userManagerProvider) {
        this.userManagerProvider = userManagerProvider;
    }

    public JahiaGroupManagerProvider getGroupManagerProvider() {
        return groupManagerProvider;
    }

    public void setGroupManagerProvider(JahiaGroupManagerProvider groupManagerProvider) {
        this.groupManagerProvider = groupManagerProvider;
    }
}
