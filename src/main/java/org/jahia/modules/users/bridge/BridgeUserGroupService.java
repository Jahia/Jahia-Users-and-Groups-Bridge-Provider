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
 *     This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.modules.external.users.ExternalUserGroupService;
import org.jahia.services.usermanager.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service for the bridge module, handle the creation/registration... for the old users and groups providers
 * @author kevan
 */
public class BridgeUserGroupService implements BundleContextAware {
    protected static final Logger logger = LoggerFactory.getLogger(BridgeUserGroupService.class);

    Map<String, BridgeUserGroupProvider> bridgeUserGroupProviderMap = new HashMap<String, BridgeUserGroupProvider>();
    BundleContext context;
    JahiaUserManagerService jahiaUserManagerService;
    JahiaGroupManagerService jahiaGroupManagerService;
    ProvidersEventHandler providersEventHandler;
    ExternalUserGroupService externalUserGroupService;

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        context = bundleContext;
    }

    public void init() {
        // register already existing providers
        for (JahiaUserManagerProvider jahiaUserManagerProvider : jahiaUserManagerService.getProviderList()){
            registerUserProvider(jahiaUserManagerProvider);
        }

        for (JahiaGroupManagerProvider jahiaUserManagerProvider : jahiaGroupManagerService.getProviderList()){
            registerGroupProvider(jahiaUserManagerProvider);
        }

        // register event handlers
        if(context != null) {
            String[] topics = new String[] {
                    BridgeEvents.USERS_GROUPS_BRIDGE_EVENT_KEY + "*"
            };

            Dictionary props = new Hashtable();
            props.put(EventConstants.EVENT_TOPIC, topics);
            context.registerService(EventHandler.class.getName(), providersEventHandler, props);
        }
    }

    public void stop() {
        for (String providerKey : bridgeUserGroupProviderMap.keySet()){
            externalUserGroupService.unregister(providerKey);
        }
        bridgeUserGroupProviderMap.clear();
    }

    /**
     * Returns the unique instance of this service.
     */
    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final BridgeUserGroupService INSTANCE = new BridgeUserGroupService();
    }

    public static BridgeUserGroupService getInstance() {
        return Holder.INSTANCE;
    }

    private BridgeUserGroupService() {
    }

    public static Logger getLogger() {
        return logger;
    }

    public void registerUserProvider(String providerKey) {
        JahiaUserManagerProvider provider = jahiaUserManagerService.getProvider(providerKey);
        if(provider != null){
            registerUserProvider(provider);
        }
    }

    public void registerGroupProvider(String providerKey) {
        JahiaGroupManagerProvider provider = jahiaGroupManagerService.getProvider(providerKey);
        if(provider != null){
            registerGroupProvider(provider);
        }
    }

    public void unregisterUserProvider(String providerKey) {
        unregisterBridge(providerKey);
    }

    public void unregisterGroupProvider(String providerKey) {
        if (bridgeUserGroupProviderMap.containsKey(providerKey)) {
            bridgeUserGroupProviderMap.get(providerKey).setGroupManagerProvider(null);
        }
    }

    public void registerUserProvider(JahiaUserManagerProvider provider) {
        registerBridge(null, provider, provider.getKey());
    }

    public void registerGroupProvider(JahiaGroupManagerProvider provider) {
        registerBridge(provider, null, provider.getKey());
    }

    private void registerBridge(JahiaGroupManagerProvider groupManagerProvider, JahiaUserManagerProvider userManagerProvider, String key){
        boolean alreadyRegistered = bridgeUserGroupProviderMap.containsKey(key);
        BridgeUserGroupProvider bridgeProvider = alreadyRegistered ? bridgeUserGroupProviderMap.get(key) : new BridgeUserGroupProvider(key);
        if(groupManagerProvider != null) {
            bridgeProvider.setGroupManagerProvider(groupManagerProvider);
        }
        if(userManagerProvider != null) {
            bridgeProvider.setUserManagerProvider(userManagerProvider);
        }
        if(!alreadyRegistered){
            bridgeUserGroupProviderMap.put(key, bridgeProvider);
            externalUserGroupService.register(key, bridgeProvider);
        }
    }

    private void unregisterBridge(String key){
        if(bridgeUserGroupProviderMap.containsKey(key)){
            bridgeUserGroupProviderMap.remove(key);
            externalUserGroupService.unregister(key);
        }
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setProvidersEventHandler(ProvidersEventHandler providersEventHandler) {
        this.providersEventHandler = providersEventHandler;
    }

    public void setExternalUserGroupService(ExternalUserGroupService externalUserGroupService) {
        this.externalUserGroupService = externalUserGroupService;
    }
}
