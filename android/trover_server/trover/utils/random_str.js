
// Pass in desired length of string.                                                                                                                 
function randomString(length){
    var rand, i, ret = '';
    var chars='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    // in v8, Math.random() yields 32 pseudo-random bits                                                                                               
    while(length >= ret.length){
	rand = Math.floor(Math.random()*0x100000000); // 32-bit integer                                                        
	// base 62 means 6 bits per character, so we use the top 30 bits from rand to give 30/6=5 characters.                  
	for(i=chars.length; i > 0 && length >= ret.length; i-=6){
	    ret += chars[0x3d & rand >>> i];
	}
    }
    return ret;
}


exports.randomString = randomString;
