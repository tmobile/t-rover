var express = require('express');
var router = express.Router();
var util = require('util');
var rs = require('../utils/random_str');
var fs = require('fs-extra');
var dateFormat = require('dateformat');


router.post('/image_capture', function(req, res, next) {
    util.log('Uploading Image.');
    if (!req.files){
	util.log("No files uploaded");
	return res.status(400).send('No files were uploaded.');
    }
    // util.log(req.files);

    var now = new Date();
    let image_file = req.files.image_file;
    if(image_file != null){
	let new_file_path = 'public/images/' + dateFormat(now, "yyyymmdd_hh_mm");
	util.log("Create Path: " + new_file_path);
	
	fs.mkdirs(new_file_path, function(err){
	    if(err)
		return res.status(500).send(err);
	    new_file_path = new_file_path + "/" + rs.randomString(16) + ".jpg";
	    util.log("Image Path: " + new_file_path);
	    util.log(new_file_path);
	    // Use the mv() method to place the file somewhere on your server
	    image_file.mv(new_file_path, function(err) {
		if (err)
		    return res.status(500).send(err);
		res.send('Happy\tTrails!');
	    });
	});
    } else {
	util.log("No Image Files.")
	return res.status(200).send("Happy Trails");
    }
});

module.exports = router;
