package org.koik.movinfo.core.db;

import org.koik.movinfo.core.db.Event;
import org.koik.movinfo.core.db.table.Table;
import org.koik.movinfo.core.db.table.Table.Record;

public interface DbChangeListener<T extends Table<T>> {
	 void registerEvent(Event e, Record<T> record);
}
