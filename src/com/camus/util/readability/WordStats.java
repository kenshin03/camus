/*
 Copyright (C) 2011 Red Soldier Limited. All rights reserved.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.camus.util.readability;

import java.util.ArrayList;
import java.util.List;

public class WordStats {
    
    private int paragraphsCount = 0;

	public int getParagraphsCount() {
        return paragraphsCount;
    }

    public void setParagraphsCount(int paragraphsCount) {
        this.paragraphsCount = paragraphsCount;
    }

	/**
	 * total number of stopwords or good words that we can calculate
	 */
	private int stopWordCount = 0;

	/**
	 * total number of words on a node
	 */
	private int wordCount = 0;

	/**
	 * holds an actual list of the stop words we found
	 */
	private List<String> stopWords = new ArrayList<String>();

	public List<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(List<String> stopWords) {
		this.stopWords = stopWords;
	}

	public int getStopWordCount() {
		return stopWordCount;
	}

	public void setStopWordCount(int stopWordCount) {
		this.stopWordCount = stopWordCount;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

}