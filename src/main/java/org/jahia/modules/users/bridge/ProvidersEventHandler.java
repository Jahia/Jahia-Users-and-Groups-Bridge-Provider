/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.usermanager.BridgeEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * Handle events related to old providers, events come from the jahia core.
 * @author kevan
 */
public class ProvidersEventHandler implements EventHandler{
    private static final Map<String, BridgeAction> eventTypeToBridgeActions = new HashMap<String, BridgeAction>(4);

    static {
        eventTypeToBridgeActions.put(BridgeEvents.USER_PROVIDER_REGISTER_BRIDGE_EVENT_KEY, new UserProviderRegisterAction());
        eventTypeToBridgeActions.put(BridgeEvents.USER_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY, new UserProviderUnregisterAction());
        eventTypeToBridgeActions.put(BridgeEvents.GROUP_PROVIDER_REGISTER_BRIDGE_EVENT_KEY, new GroupProviderRegisterAction());
        eventTypeToBridgeActions.put(BridgeEvents.GROUP_PROVIDER_UNREGISTER_BRIDGE_EVENT_KEY, new GroupProviderUnregisterAction());
    }

    @Override
    public void handleEvent(Event event) {
        BridgeAction bridgeAction = eventTypeToBridgeActions.get(event.getTopic());
        if(bridgeAction != null) {
            String provider = (String) event.getProperty(BridgeEvents.PROVIDER_KEY);
            if(StringUtils.isNotEmpty(provider)){
                bridgeAction.doAction(provider);
            }
        }
    }

    public abstract static class BridgeAction {
        Logger getLogger(){
            return BridgeUserGroupService.getLogger();
        }

        BridgeUserGroupService getInstance(){
            return BridgeUserGroupService.getInstance();
        }

        abstract void doAction(String providerKey);
    }

    public static class GroupProviderRegisterAction extends BridgeAction {
        @Override
        public void doAction(String providerKey) {
            getLogger().debug("register old group provider: " + providerKey);
            getInstance().registerGroupProvider(providerKey);
        }
    }

    public static class GroupProviderUnregisterAction extends BridgeAction {
        @Override
        public void doAction(String providerKey) {
            getLogger().debug("Unregister old group provider: " + providerKey);
            getInstance().unregisterGroupProvider(providerKey);
        }
    }

    public static class UserProviderRegisterAction extends BridgeAction {
        @Override
        public void doAction(String providerKey) {
            getLogger().debug("Register old user provider: " + providerKey);
            getInstance().registerUserProvider(providerKey);
        }
    }

    public static class UserProviderUnregisterAction extends BridgeAction {
        @Override
        public void doAction(String providerKey) {
            getLogger().debug("Unregister old user provider: " + providerKey);
            getInstance().unregisterUserProvider(providerKey);
        }
    }
}
