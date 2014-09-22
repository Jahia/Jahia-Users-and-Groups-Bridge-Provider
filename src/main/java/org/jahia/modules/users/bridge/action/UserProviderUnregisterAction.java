package org.jahia.modules.users.bridge.action;

import org.jahia.modules.users.bridge.BridgeUserGroupService;

/**
 * @author kevan
 */
public class UserProviderUnregisterAction implements BridgeAction{
    @Override
    public void doAction(String providerKey) {
        BridgeUserGroupService.getLogger().debug("Unregister old user provider: " + providerKey);
        BridgeUserGroupService.getInstance().unregisterUserProvider(providerKey);
    }
}
