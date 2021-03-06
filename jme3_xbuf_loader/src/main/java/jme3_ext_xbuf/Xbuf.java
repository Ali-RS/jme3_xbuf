package jme3_ext_xbuf;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

import com.google.protobuf.ExtensionRegistry;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import jme3_ext_xbuf.mergers.AnimationsMerger;
import jme3_ext_xbuf.mergers.PhysicsMerger;
import jme3_ext_xbuf.mergers.CustomParamsMerger;
import jme3_ext_xbuf.mergers.LightsMerger;
import jme3_ext_xbuf.mergers.MaterialsMerger;
import jme3_ext_xbuf.mergers.MeshesMerger;
import jme3_ext_xbuf.mergers.NodesMerger;
import jme3_ext_xbuf.mergers.RelationsMerger;
import jme3_ext_xbuf.mergers.SkeletonsMerger;
import xbuf.Datas.Data;
import xbuf_ext.AnimationsKf;
import xbuf_ext.CustomParams;

public class Xbuf{
	public final ExtensionRegistry registry;
	public final List<Merger> mergers;
	/**
	 * A full constructor that allow to define every service (to injection).
	 * @param assetManager the AssetManager used to load assets (texture, sound,...)
	 * @param registry the protobuf registry for extensions
	 * @param loader4Materials the xbuf way to load materials (null == use default implementation)
	 * @param loader4Relations the xbuf way to load relations (null == use default implementation)
	 */
	public Xbuf(AssetManager assetManager, ExtensionRegistry registry, MaterialsMerger loader4Materials, RelationsMerger loader4Relations){
		loader4Materials = (loader4Materials != null) ?loader4Materials : new MaterialsMerger(assetManager);
		loader4Relations = (loader4Relations != null) ?loader4Relations : new RelationsMerger(loader4Materials);
		mergers=new LinkedList<Merger>();
		mergers.add(new NodesMerger());
		mergers.add(new MeshesMerger(loader4Materials));
		mergers.add(loader4Materials);
		mergers.add(new LightsMerger());
		mergers.add(new SkeletonsMerger());
		mergers.add(new AnimationsMerger());
		mergers.add(new CustomParamsMerger());
		mergers.add(new PhysicsMerger());

		// relations should be the last because it reuse data provide by other (put in components)
		mergers.add(loader4Relations);

		this.registry=registry!=null?registry:ExtensionRegistry.newInstance();
		setupExtensionRegistry(this.registry);
	}

	public Xbuf(AssetManager assetManager){
		this(assetManager,null,null,null);
	}

	protected ExtensionRegistry setupExtensionRegistry(ExtensionRegistry r) {
		CustomParams.registerAllExtensions(r);
		AnimationsKf.registerAllExtensions(r);
		return r;
	}

	// TODO optimize to create less intermediate node
	public void merge(Data src, Node root, XbufContext context, Logger log) throws Exception {
		for(Merger m:mergers){
			log.debug("{} begin", m.getClass());
			long beginAt = System.currentTimeMillis();
			m.apply(src,root,context,log);
			long endAt = System.currentTimeMillis();
			log.info("{} end: duration {} ms", m.getClass(), endAt - beginAt);
		}
	}
}
