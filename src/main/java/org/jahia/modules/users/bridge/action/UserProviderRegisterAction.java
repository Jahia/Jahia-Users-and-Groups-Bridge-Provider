package org.jahia.modules.users.bridge.action;

import org.jahia.modules.users.bridge.BridgeUserGroupService;

/**
 * @author kevan
 */
public class UserProviderRegisterAction implements BridgeAction{
    @Override
    public void doAction(String providerKey) {
        BridgeUserGroupService.getLogger().debug("Register old user provider: " + providerKey);
        BridgeUserGroupService.getInstance().registerUserProvider(providerKey);
    }
}
