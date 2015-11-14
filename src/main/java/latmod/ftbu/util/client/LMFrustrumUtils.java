package latmod.ftbu.util.client;

import ftb.lib.client.FTBLibClient;
import latmod.lib.LMUtils;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class LMFrustrumUtils
{
	public static boolean isFirstPerson;
	public static int currentDim;
	public static double playerX, playerY, playerZ, renderX, renderY, renderZ;
	public static final Frustum frustrum = new Frustum();
	public static long playerPosHash;
	
	/*
	public static final IntBuffer viewport = BufferUtils.createIntBuffer(4);
	public static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
	public static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
	*/
	
	public static void update()
	{
		isFirstPerson = FTBLibClient.mc.gameSettings.thirdPersonView == 0;
		currentDim = LatCoreMCClient.getDim();
		RenderManager rm = FTBLibClient.mc.getRenderManager();
		playerX = rm.viewerPosX;
		playerY = rm.viewerPosY;
		playerZ = rm.viewerPosZ;
		//FIXME: Render pos
		renderX = 0;//rm.renderPosX;
		renderY = 0;//rm.renderPosY;
		renderZ = 0;//rm.renderPosZ;
		playerPosHash = Math.abs(LMUtils.longHashCode(currentDim, playerX, playerY, playerZ) + 1);
		frustrum.setPosition(playerX, playerY, playerZ);
	}
	
	/*
	public static void updateMatrix()
	{
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
	}
	
	public static Point2D getScreenCoords(double x, double y, double z)
	{
		FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
		//FloatBuffer screenCoords = BufferUtils.createFloatBuffer(4);
		
		boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
		if(result)
		{
			float px = screenCoords.get(0);
			float py = screenCoords.get(1) - screenCoords.get(2);
			
			//if(Minecraft == 0) System.out.println(px + " : " + py);
			
			if(px >= 0 && py >= 0 && px < viewport.get(2) && py < viewport.get(3))
				return new Point2D(px, py);
		}
		return null;
	}
	
	*/
}