package org.koik.movinfo.provider;

import java.io.File;
import java.util.List;

import org.koik.movinfo.core.Service;
import org.koik.movinfo.util.Pair;

public interface Sink
		extends
		Service<Pair<File, List<OMDbResult>>, Void> {
}
