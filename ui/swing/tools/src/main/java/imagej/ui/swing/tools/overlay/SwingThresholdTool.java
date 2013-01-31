/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.tools.overlay;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.plugin.Plugin;
import imagej.ui.swing.overlay.AbstractJHotDrawAdapter;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.SwingThresholdFigure;

import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.jhotdraw.draw.Figure;

// TODO - add event handlers so that mappings can go away when things closed.
// Otherwise this is a source of resource leaks.

/**
 * Swing/JHotDraw implementation of threshold tool.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = JHotDrawAdapter.class, name = "Threshold",
	description = "Threshold overlay",
	priority = 1000)
public class SwingThresholdTool extends
	AbstractJHotDrawAdapter<ThresholdOverlay, SwingThresholdFigure>
{
	private static List<Mapping> mappings = new LinkedList<Mapping>();
	private static ImgPlus<? extends RealType<?>> defaultImgPlus = null;
	
	private class Mapping {
		ImgPlus<? extends RealType<?>> imgPlus;
		ThresholdOverlay overlay;
		SwingThresholdFigure figure;
	}
	
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if (!(overlay instanceof ThresholdOverlay)) return false;
		return figure == null || figure instanceof SwingThresholdFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		ImageDisplay display = getDisplay();
		ImgPlus<? extends RealType<?>> imgPlus = getImgPlus(display);
		for (Mapping m : mappings) {
			if (m.imgPlus == imgPlus) return m.overlay;
		}
		Mapping m = map(display, imgPlus);
		return m.overlay;
	}

	@Override
	public Figure createDefaultFigure() {
		ImageDisplay display = getDisplay();
		ImgPlus<? extends RealType<?>> imgPlus = getImgPlus(display);
		for (Mapping m : mappings) {
			if (m.imgPlus == imgPlus) return m.figure;
		}
		Mapping m = map(display, imgPlus);
		return m.figure;
	}

	@Override
	public JHotDrawTool getCreationTool(ImageDisplay display) {
		return new IJCreationTool<SwingThresholdFigure>(display, this);
	}

	@Override
	public Shape toShape(SwingThresholdFigure figure) {
		throw new UnsupportedOperationException("Unimplemented");
	}

	private ImageDisplay getDisplay() {
		ImageDisplayService service =
			getContext().getService(ImageDisplayService.class);
		return service.getActiveImageDisplay();
	}

	private ImgPlus<? extends RealType<?>> getImgPlus(ImageDisplay display) {
		ImageDisplayService service = getContext().getService(ImageDisplayService.class);
		Dataset ds = service.getActiveDataset(display);
		if (ds != null) return ds.getImgPlus();
		return getDefaultImgPlus();
	}
	
	private ImgPlus<? extends RealType<?>> getDefaultImgPlus() {
		if (defaultImgPlus == null) {
			// make a phantom dataset so we can always have an imgplus for this tool
			DatasetService dss = getContext().getService(DatasetService.class);
			Dataset dataset =
				dss.create(new UnsignedByteType(), new long[] { 1, 1 },
					"DefaultImgPlus", new AxisType[] { Axes.X, Axes.Y });
			defaultImgPlus = dataset.getImgPlus();
		}
		return defaultImgPlus;
	}

	private Mapping map(ImageDisplay display,
		ImgPlus<? extends RealType<?>> imgPlus)
	{
		Mapping m = new Mapping();
		m.imgPlus = imgPlus;
		m.overlay = new ThresholdOverlay(getContext(), imgPlus);
		m.figure = new SwingThresholdFigure(display, imgPlus, m.overlay);
		initDefaultSettings(m.figure);
		m.overlay.setFigure(m.figure);
		mappings.add(m);
		return m;
	}

}
