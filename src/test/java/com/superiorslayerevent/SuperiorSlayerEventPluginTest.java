package com.superiorslayerevent;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SuperiorSlayerEventPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SuperiorSlayerEventPlugin.class);
		RuneLite.main(args);
	}
}