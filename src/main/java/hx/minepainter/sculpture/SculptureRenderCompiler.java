package hx.minepainter.sculpture;

import hx.minepainter.ModMinePainter;
import hx.utils.Debug;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;

@SideOnly(Side.CLIENT)
public class SculptureRenderCompiler {
	public static RenderBlocks rb = new SculptureRenderBlocks();

	int glDisplayList = -1;
	int light;
	boolean changed = false;
	
	public void updateLight(int light){
		if(light != this.light)
			changed = true;
		this.light = light;
	}	
	
	public boolean update(BlockSlice slice){
		if(glDisplayList != -1 && !changed)return false;
		
		if(glDisplayList < 0)glDisplayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(glDisplayList, GL11.GL_COMPILE);
		build(slice);
		GL11.glEndList();
		
		changed = false;
		return true;
	}
	
	public void build(BlockSlice slice){
		rb.blockAccess = slice;
		rb.renderAllFaces = false;
		SculptureBlock sculpture = ModMinePainter.sculpture.block;
		
		TextureManager tm = Minecraft.getMinecraft().renderEngine;
		tm.bindTexture(TextureMap.locationBlocksTexture);
		
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.setTranslation(0d,0d,0d);
		
		for(int i = 0; i < 512; i ++){
			int x = (i >> 6) & 7;
			int y = (i >> 3) & 7;
			int z = (i >> 0) & 7;

			Block b = slice.getBlock(x, y, z);
			if(b == Blocks.air)continue;
			int meta = slice.getBlockMetadata(x, y, z);
			sculpture.setCurrentBlock(b, meta);
			sculpture.setSubCoordinate(x,y,z);
			
			tes.setTranslation(-x, -y, -z);
//			rb.renderStandardBlock(ModMinePainter.sculpture.block, x,y,z);
			sculpture.setBlockBounds(x/8f, y/8f, z/8f, (x+1)/8f, (y+1)/8f, (z+1)/8f);
			rb.renderBlockByRenderType(sculpture, x,y,z);
		}
		
		ModMinePainter.sculpture.block.setCurrentBlock(null,0);
		sculpture.setBlockBounds(0,0,0,1,1,1);
		rb.blockAccess = null;
		tes.draw();
	}

	public void clear(){
		if(glDisplayList>=0)
			GL11.glDeleteLists(glDisplayList, 1);
	}


	public boolean ready() {
		return glDisplayList>=0;
	}
}
