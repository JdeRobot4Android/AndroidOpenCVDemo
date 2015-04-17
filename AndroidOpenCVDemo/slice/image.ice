/*
 *
 *  Copyright (C) 1997-2010 JDE Developers Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *
 *  Author : David Lobato Bravo <dav.lobato@gmail.com>
 *	     Sara Marugán Alonso <smarugan@gsyc.es>
 *
 */

#ifndef IMAGE_ICE
#define IMAGE_ICE

#include "common.ice"


module jderobot{  


  /**
   *  Static description of the image source.
   */
  class ImageDescription 
  {
    int width; /**< %Image width [pixels] */
    int height;/**< %Image height [pixels] */
    int size;/**< %Image size [bytes] */
    string format; /**< %Image format string */
  };
  

  /**
   * A single image served as a sequence of bytes
   */
  class ImageData
  { 
    Time timeStamp; /**< TimeStamp of Data */
    ImageDescription description; /**< ImageDescription of Data, for convienence purposes */
    ByteSeq pixelData; /**< The image data itself. The structure of this byte sequence
			  depends on the image format and compression. */
  };


  //! Interface to the image consumer.
  interface ImageConsumer
  {
    //! Transmits the data to the consumer.
    void report( ImageData obj );
  };


  /** 
   * Interface to the image provider.
   */
  interface ImageProvider
  {
    /** 
     * Returns the image source description.
     */
    idempotent ImageDescription getImageDescription();

    /**
     * Returns the latest data.
     */
    ["amd"] idempotent ImageData getImageData()
      throws DataNotExistException, HardwareFailedException;
  };

}; //module

#endif //IMAGE_ICE