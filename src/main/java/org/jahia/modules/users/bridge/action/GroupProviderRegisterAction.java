package org.jahia.modules.users.bridge.action;

import org.jahia.modules.users.bridge.BridgeUserGroupService;

/**
 * @author kevan
 */
public class GroupProviderRegisterAction implements BridgeAction{
    @Override
    public void doAction(String providerKey) {
        BridgeUserGroupService.getLogger().debug("register old group provider: " + providerKey);
        BridgeUserGroupService.getInstance().registerGroupProvider(providerKey);
    }
}
