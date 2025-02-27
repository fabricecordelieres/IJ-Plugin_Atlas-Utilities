/**
 * JsonRoiEntry.java
 * 
 * Created on 4 août 2023
 * Fabrice P. Cordelieres, fabrice.cordelieres at gmail.com
 * 
 * Copyright (C) 2023 Fabrice P. Cordelieres
 *
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

/**
 * This class provides a way to personalize serialization/deserialization of a Roi object.
 */
public class jsonRoi implements JsonSerializer<Roi>, JsonDeserializer<Roi>{

	@Override
	public JsonElement serialize(Roi roi, Type typeOfSrc, JsonSerializationContext context) {
		if (roi==null) return null;

		//Transforms roi into string from byte array
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream(4096);
			RoiEncoder encoder = new RoiEncoder(bao);
			encoder.write(roi);
			bao.close();
			bytes = bao.toByteArray(); 
		} catch (IOException e) {
			return null;
		}

		JsonObject job=new JsonObject();
		job.addProperty("roi", DatatypeConverter.printBase64Binary(bytes));

		return job;
	}

	@Override
	public Roi deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		if (json==null) return null;

		JsonObject jsonObject = json.getAsJsonObject();
		Roi roi=RoiDecoder.openFromByteArray(DatatypeConverter.parseBase64Binary(jsonObject.get("roi").getAsString()));

		//TODO: implement mechanism for measurements entry or only do parsing for ij.Roi

		return roi;
	}
}
