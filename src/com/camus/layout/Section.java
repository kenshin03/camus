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

package com.camus.layout;

import java.util.List;

public class Section {
    private List<Feature> featuredStories;
    private List<Filler> fillerStories;
    private List<ShortMessage> shortMessages;
    public List getFeaturedStories() {
        return featuredStories;
    }
    public void setFeaturedStories(List featuredStories) {
        this.featuredStories = featuredStories;
    }
    public List getFillerStories() {
        return fillerStories;
    }
    public void setFillerStories(List fillerStories) {
        this.fillerStories = fillerStories;
    }
    public List getShortMessages() {
        return shortMessages;
    }
    public void setShortMessages(List shortMessages) {
        this.shortMessages = shortMessages;
    }
    
}
