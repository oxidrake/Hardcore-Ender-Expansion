package chylex.hee.mechanics.compendium.render;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import chylex.hee.game.save.types.player.CompendiumFile;
import chylex.hee.gui.GuiEnderCompendium;
import chylex.hee.mechanics.compendium.content.KnowledgeFragment;
import chylex.hee.mechanics.compendium.content.KnowledgeObject;
import chylex.hee.mechanics.compendium.content.objects.IObjectHolder;

public class ObjectDisplayElement{
	private enum BackgroundTile{
		PLAIN(113,0), DISABLED(113,23), CHECKERED(136,0), BRIGHT(136,23), GOLD(159,0);
		
		final byte x, y;
		
		BackgroundTile(int x, int y){
			this.x = (byte)x;
			this.y = (byte)y;
		}
	}
	
	public final KnowledgeObject<? extends IObjectHolder<?>> object;
	public final int y;
	
	public ObjectDisplayElement(KnowledgeObject<? extends IObjectHolder<?>> object, int y){
		this.object = object;
		this.y = y;
	}
	
	public void render(GuiScreen gui, CompendiumFile compendiumFile, int yLowerBound, int yUpperBound){
		int x = GuiEnderCompendium.guiObjLeft+object.getX(), y = this.y+object.getY();
		if (y < yLowerBound || y > yUpperBound)return;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1F,1F,1F,1F);
		
		BackgroundTile tile = BackgroundTile.DISABLED;
		
		if (compendiumFile.hasDiscoveredObject(object)){
			boolean hasAll = true;
			
			for(KnowledgeFragment fragment:object.getFragments()){
				if (!compendiumFile.hasUnlockedFragment(fragment)){
					hasAll = false;
					break;
				}
			}
			
			tile = hasAll ? BackgroundTile.GOLD : BackgroundTile.PLAIN;
		}
		
		RenderHelper.disableStandardItemLighting();
		gui.mc.getTextureManager().bindTexture(GuiEnderCompendium.texBack);
		gui.drawTexturedModalRect(x,y,tile.x,tile.y,22,22);
		RenderHelper.enableGUIStandardItemLighting();
		GuiEnderCompendium.renderItem.renderItemIntoGUI(gui.mc.fontRenderer,gui.mc.getTextureManager(),object.holder.getDisplayItemStack(),x+3,y+3,true);
	}
	
	public boolean isMouseOver(int mouseX, int mouseY, int offsetY){
		int x = GuiEnderCompendium.guiObjLeft+object.getX(), y = this.y+object.getY()+offsetY;
		return mouseX >= x && mouseY >= y && mouseX <= x+20 && mouseY <= y+20;
	}
}