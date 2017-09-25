$(document).ready(function () {
    $("#login").click(function () {
        var cus = {
            username: $('#username1').val(),
            password: $('#password1').val()
        }

        $.ajax({
            url: '/login',
            type: 'post',
            data: JSON.stringify(cus),
            dataType: 'text',
            contentType: 'application/json',
            success: function(data){
                var obj = JSON.parse(data)
                //alert(obj.valid)
                
                if(obj.valid) {
                    document.location.href='/welcome'
                }
                else{
                    alert("Wrong username/password or your accout was blocked")
                }
            },
            fail: function(data){
                alert(data)
            }
        });
    })
    
    $("#signup").click(function () {
        var cus = {
            username: $('#username2').val(),
            email   : $('#email').val(),
            password: $('#password2').val()
        }

        $.ajax({
            url: '/signup',
            type: 'post',
            data: JSON.stringify(cus),
            dataType: 'text',
            contentType: 'application/json',
            success: function(data){
                var obj = JSON.parse(data)
                
                if(obj.valid) {
                    document.location.href='/welcome'
                }else{
                    alert("This username or email has already existed")
                }
            },
            fail: function(data){
                alert(data)
            }
        });
    })
});