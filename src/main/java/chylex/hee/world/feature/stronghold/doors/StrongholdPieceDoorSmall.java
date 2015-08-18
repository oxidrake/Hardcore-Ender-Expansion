package chylex.hee.world.feature.stronghold.doors;
import java.util.Random;
import chylex.hee.system.abstractions.Pos.PosMutable;
import chylex.hee.world.structure.StructureWorld;
import chylex.hee.world.structure.dungeon.StructureDungeonPieceInst;
import chylex.hee.world.structure.util.Facing4;

public class StrongholdPieceDoorSmall extends StrongholdPieceDoor{
	public static StrongholdPieceDoorSmall[] generateDoors(){
		return new StrongholdPieceDoorSmall[]{
			new StrongholdPieceDoorSmall(Facing4.EAST_POSX),
			new StrongholdPieceDoorSmall(Facing4.SOUTH_POSZ)
		};
	}
	
	public StrongholdPieceDoorSmall(Facing4 facing){
		super(facing);
	}

	@Override
	protected void generateDoor(StructureDungeonPieceInst inst, StructureWorld world, Random rand, int x, int y, int z){
		PosMutable archPos = new PosMutable(x+maxX/2,0,z+maxZ/2);
		Facing4 perpendicular = facing.perpendicular();
		
		archPos.move(perpendicular,-1);
		placeLine(world,rand,placeStoneBrick,archPos.x,y+1,archPos.z,archPos.x,y+3,archPos.z);
		archPos.move(perpendicular,1);
		placeBlock(world,rand,placeStoneBrick,archPos.x,y+3,archPos.z);
		archPos.move(perpendicular,1);
		placeLine(world,rand,placeStoneBrick,archPos.x,y+1,archPos.z,archPos.x,y+3,archPos.z);
	}
}
