/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.rest.utils.collection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.wicketstuff.rest.resource.urlsegments.AbstractURLSegment;
import org.wicketstuff.rest.resource.urlsegments.MultiParamSegment;
import org.wicketstuff.rest.resource.urlsegments.ParamSegment;

/**
 * Wrapper class for {@link java.util.Iterator} meant to recursively
 * iterate over its elements if they are iterable.
 * 
 * @author Andrea Del Bene
 */
public class SegmentIterator implements Iterator<AbstractURLSegment>
{
	private Iterator<AbstractURLSegment> currentIterator;
	private Deque<Iterator<AbstractURLSegment>> iteratorStack = new ArrayDeque<>();
	
	public SegmentIterator(Iterator<AbstractURLSegment> currentIterator)
	{
		this.currentIterator = currentIterator;
	}
	

	@Override
	public boolean hasNext()
	{
		if (!currentIterator.hasNext() && !iteratorStack.isEmpty())
		{
			currentIterator = iteratorStack.pop();
		}
		
		return currentIterator.hasNext();
	}

	@Override
	public AbstractURLSegment next()
	{
		AbstractURLSegment nextElement = currentIterator.next();
		
		if (nextElement instanceof MultiParamSegment)
		{
			iteratorStack.push(currentIterator);
			currentIterator = ((MultiParamSegment)nextElement)
				.getSubSegments().iterator();
			
			return currentIterator.next();
		}
		
		return nextElement;
	}

	public String nextParamName()
	{
		String nextParamName = null;
		while (hasNext() && nextParamName == null)
		{
			AbstractURLSegment nextSegment = next();
			
			if (nextSegment instanceof ParamSegment)
			{
				ParamSegment paramSegment = (ParamSegment)nextSegment;
				nextParamName = paramSegment.getParamName();
			}
		}
		
		return nextParamName;
	}

}
