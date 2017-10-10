/**
Copyright 2017 University of Lille

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package fr.univLille.cristal.shex.schema.concrsynt;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import fr.univLille.cristal.shex.validation.Configuration;

/**
 * 
 * @author Iovka Boneva
 * 10 oct. 2017
 */
public class StringFacetSetOfNodes implements SetOfNodes {
	
	private Integer length, minlength, maxlength;
	//private Pattern pattern;
	private String patternString;
		
	public void setLength(Integer length) {
		if (this.length == null)
			this.length = length;
		else throw new IllegalStateException("length already set");
	}
	public void setMinLength(Integer minlength) {
		if (this.minlength == null)
			this.minlength = minlength;
		else throw new IllegalStateException("minlength already set");
	}
	public void setMaxLength(Integer maxlength) {
		if (this.maxlength == null)
			this.maxlength = maxlength;
		else throw new IllegalStateException("maxlength already set");
	}
	public void setPattern(String patternString) {
		if (patternString == null)
			return;
		if (this.patternString == null)
			this.patternString = patternString;
		else throw new IllegalStateException("pattern already set");
	}
	@Override
	public boolean contains(Value node) {
		String lex = null;
		if (node instanceof Literal)
			lex = ((Literal)node).stringValue();
		else if (node instanceof IRI)
			lex = ((IRI)node).stringValue();
		else if (node instanceof BNode)
			lex = ((BNode)node).getID();
		
		if (patternString != null && ! Configuration.getXMLSchemaRegexMatcher().matches(lex, patternString))
			return false;
		if (length != null && lex.length() != length)
			return false;
		if (minlength != null && lex.length() < minlength)
			return false;
		if (maxlength != null && lex.length() > maxlength)
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		String len = length == null ? ""    :  " length: " + length.toString();
		String min = minlength == null ? "" :  " minlength: " + minlength.toString();
		String max = maxlength == null ? "" :  " maxlength: " + maxlength.toString();
		String pat = patternString == null ? ""   :  " pattern: " + patternString.toString();
		return len + min + max + pat;
	}
}