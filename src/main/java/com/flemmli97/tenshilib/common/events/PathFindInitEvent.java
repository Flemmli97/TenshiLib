package com.flemmli97.tenshilib.common.events;

import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PathFindInitEvent extends Event{

	private final PathNavigate navigator;
	private PathFinder pathFinder;
	
	public PathFindInitEvent(PathNavigate navigator, PathFinder pathFinder)
	{
		this.navigator=navigator;
		this.pathFinder=pathFinder;
	}
	
	public PathNavigate getNavigator()
	{
		return this.navigator;
	}
	
	public PathFinder getPathFinder()
	{
		return this.pathFinder;
	}
	
	public void setPathFinder(PathFinder pathFinder)
	{
		this.pathFinder=pathFinder;
	}
}
