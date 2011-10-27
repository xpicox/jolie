/***************************************************************************
 *   Copyright (C) 2008 by Fabrizio Montesi <famontesi@gmail.com>          *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/


/**
WARNING: work in progress, the API is unstable.
*/

type Date:void {
	.day:int
	.month:int
	.year:int
}

type DateValuesType:void {
	.day:int
	.month:int
	.year:int
}

type Time:void {
	.hour:int
	.minute:int
	.second:int
}

type TimeValuesType:void {
	.hour:int
	.minute:int
	.second:int
}

type DateValuesRequestType:string {
	/**
		Date format.
		If not specified, it defaults to "dd/MM/yyyy"
	*/
	.format?:string
}

type CurrentDateTimeRequestType:void {
	/**
		Date format.
		If not specified, it defaults to "dd/MM/yyyy"
	*/
	.format?:string
}

type DiffDateRequestType:void {
	.format?:string
	.date1:string
	.date2:string
}
type GetTimeDiffRequest:void {
	.time1:string
	.time2:string
}
outputPort Time {
OneWay:
	setNextTimeout, setNextTimeoutByDateTime, setNextTimeoutByTime
RequestResponse:
	getCurrentDateTime(CurrentDateTimeRequestType)(string), sleep,

	/**
		Converts an input string into a date expressed by means of
		three elements: day, month and year. The request may specify the 
		date parsing format. See #DateValuesRequestType for details.
	*/
	getDateValues(DateValuesRequestType)(DateValuesType),

	/**
		Returns the current date split in three fields: day, month and year
	*/
	getCurrentDateValues(void)(DateValuesType),
	getDateDiff(DiffDateRequestType)(int),

	/**!
	Warning: this is temporary and subject to future change as soon as long is supported by Jolie.
	*/
	getCurrentTimeMillis(void)(string),
	getTimeValues(string)(TimeValuesType),
	getTimeDiff(GetTimeDiffRequest)(int)
	getTimeFromMilliSeconds(int)(TimeValuesType);
}

embedded {
Java:
	"joliex.util.TimeService" in Time
}
