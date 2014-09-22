package org.jahia.modules.users.bridge.action;

import org.jahia.modules.users.bridge.BridgeUserGroupService;

/**
 * @author kevan
 */
public class GroupProviderUnregisterAction implements BridgeAction {
    @Override
    public void doAction(String providerKey) {
        BridgeUserGroupService.getLogger().debug("Unregister old group provider: " + providerKey);
        BridgeUserGroupService.getInstance().unregisterGroupProvider(providerKey);
    }
}
