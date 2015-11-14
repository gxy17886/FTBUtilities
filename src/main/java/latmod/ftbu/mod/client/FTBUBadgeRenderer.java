package latmod.ftbu.mod.client;

import ftb.lib.EventBusHelper;
import latmod.ftbu.badges.*;
import latmod.ftbu.world.*;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class FTBUBadgeRenderer
{
	public static final FTBUBadgeRenderer instance = new FTBUBadgeRenderer();
	public static boolean isEnabled = false;
	
	public void enable(boolean enable)
	{
		if(isEnabled != enable)
		{
			isEnabled = true;
			
			if(enable) EventBusHelper.register(this);
			else EventBusHelper.unregister(this);
		}
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Specials.Post e)
	{
		if(!Badge.badges.isEmpty() && FTBUClient.renderBadges.getB() && !e.entityPlayer.isInvisible())
		{
			LMPlayerClient pc = LMWorldClient.inst.getPlayer(e.entityPlayer);
			
			if(pc != null && pc.settings.renderBadge)
			{
				if(pc.cachedBadge == null)
				{
					pc.cachedBadge = Badge.badges.get(pc.getUUID());
					if(pc.cachedBadge == null) pc.cachedBadge = new BadgeEmpty();
				}
				
				pc.cachedBadge.onPlayerRender(e.entityPlayer);
			}
		}
	}
}