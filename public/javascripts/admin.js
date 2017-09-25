$(document).ready(function () {
    
    $('#add').click(function () {
        var cus = {
            username: $('#username1').val(),
            email   : $('#email').val(),
            password: $('#password').val()
        }

        $.ajax({
            url: '/add',
            type: 'post',
            data: JSON.stringify(cus),
            dataType: 'text',
            contentType: 'application/json',
            success: function(data){
                var obj = JSON.parse(data)
                
                if(obj.valid) {
                    document.location.href='/welcome'
                }
                else {
                    alert("This username or email has already existed")
                }
            },
            fail: function(data){
                alert("fail")
            }
        });
    })
    
   $('#delete').click(function () {
        var cus = {
            username: $('#username2').val()
        }
        
        //alert("hahahaha")
        $.ajax({
            url: '/delete',
            type: 'post',
            data: JSON.stringify(cus),
            dataType: 'text',
            contentType: 'application/json',
            success: function(data){
                var obj = JSON.parse(data)
                
                if(obj.valid) {
                    document.location.href='/welcome'
                }
                else {
                    alert("This username doesn't exist")
                }
            },
            fail: function(data){
                alert("fail")
            }
        })
    })
    
    $('#block').click(function () {
        var cus = {
            username: $('#username3').val()
        }
        
        $.ajax({
            url: '/block',
            type: 'post',
            data: JSON.stringify(cus),
            dataType: 'text',
            contentType: 'application/json',
            success: function(data){
                var obj = JSON.parse(data)
                
                if(obj.valid) {
                    document.location.href='/welcome'
                }
                else {
                    alert("This username doesn't exist")
                }
            },
            fail: function(data){
                alert("fail")
            }
        })
    })
    
    $('#logout').click(function () {
        document.location.href='/logout'
    })
});