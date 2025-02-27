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

import dataModel.MeasurementsEntry;
import dataModel.RoiEntry;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

/**
 * This class provides a way to personalize serialization/deserialization of a RoiEntry object.
 */
public class jsonRoiEntry implements JsonSerializer<RoiEntry>, JsonDeserializer<RoiEntry>{

	@Override
	public JsonElement serialize(RoiEntry re, Type typeOfSrc, JsonSerializationContext context) {
		if (re==null) return null;

		//Transforms roi into string from byte array
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream(4096);
			RoiEncoder encoder = new RoiEncoder(bao);
			encoder.write(re.roi);
			bao.close();
			bytes = bao.toByteArray(); 
		} catch (IOException e) {
			return null;
		}

		JsonObject job=new JsonObject();
		job.addProperty("slice", re.slice);
		job.addProperty("roi", DatatypeConverter.printBase64Binary(bytes));
		
		JsonObject measurements=new JsonObject();
		measurements.addProperty("area", re.Measurements.area);
		measurements.addProperty("totalIntensity", re.Measurements.totalIntensity);
		measurements.addProperty("meanIntensity", re.Measurements.meanIntensity);
		measurements.addProperty("stdIntensity", re.Measurements.stdIntensity);
		measurements.addProperty("minIntensity", re.Measurements.minIntensity);
		measurements.addProperty("maxIntensity", re.Measurements.maxIntensity);
		
		job.add("Measurements", measurements);
		return job;
	}

	@Override
	public RoiEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		if (json==null) return null;

		JsonObject jsonObject = json.getAsJsonObject();
		int slice=jsonObject.get("slice").getAsInt();
		Roi roi=RoiDecoder.openFromByteArray(DatatypeConverter.parseBase64Binary(jsonObject.get("roi").getAsString()));
		
		RoiEntry re=new RoiEntry(slice, roi);
		MeasurementsEntry me=new MeasurementsEntry();
		
		JsonObject measurements=jsonObject.getAsJsonObject("Measurements");
		if(measurements!=null) {
			me.area=measurements.get("area").getAsDouble();
			me.totalIntensity=measurements.get("totalIntensity").getAsDouble();
			me.meanIntensity=measurements.get("meanIntensity").getAsDouble();
			me.stdIntensity=measurements.get("stdIntensity").getAsDouble();
			me.minIntensity=measurements.get("minIntensity").getAsDouble();
			me.maxIntensity=measurements.get("maxIntensity").getAsDouble();
		}
		
		re.Measurements=me;
		
		return re;
	}
}
