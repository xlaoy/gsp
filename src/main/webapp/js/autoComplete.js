function mSift(cVarName){
	this.oo=cVarName;
}

mSift.prototype={
    url: null,
    prama:null,
	Varsion:'zhangyijun',
	ValTarget:Object,
	Target:Object,
	TgList:Object,
	Listeners:null,
	SelIndex:0,
	Data:[],
	ReData:[],
	returnCode: null,
	returnName: null,
	//
	Create:function(parma){
		var _this=this;
		
		_this.url = parma.url;
		_this.prama = parma.prama;
		_this.ValTarget = parma.codeObj;
		_this.Target = parma.nameObj;
		_this.returnCode = parma.returnCode;
		_this.returnName = parma.returnName;
		
		oObj = parma.nameObj;
		
		var oUL=document.createElement('ul');
		oUL.style.cssText='list-style: none;';
		oUL.style.display='none';
		oObj.parentNode.insertBefore(oUL,oObj);
		_this.TgList=oUL;
		oObj.onkeydown=oObj.onclick=function(event){
			_this.Listen(this, event);
		};
		oObj.onblur=function(){
			setTimeout(function(){
				_this.Clear();
			},100);
		};
	},
	//
	Complete:function(date){
		var _this=this;
		_this.ValTarget.value = _this.returnCode(date);
	},
	//
	Select:function(){
		var _this=this;
		if(_this.ReData.length>0){
			_this.Target.value=_this.returnName(_this.ReData[_this.SelIndex]);
			_this.Complete(_this.ReData[_this.SelIndex]);
			_this.Clear();
		}
		setTimeout(function(){_this.Target.focus();},10);
	},
	//
	Listen:function(oObj, event){
		var _this=this;
		var ev=window.event||event;
		switch(ev.keyCode){
			case 9://TAB
					return;
			case 13://ENTER
					_this.Target.blur();
					_this.Select();
					return;
			case 38://UP
					_this.SelIndex=_this.SelIndex>0?_this.SelIndex-1:_this.ReData.length-1;
					break;
			case 40://DOWN
					_this.SelIndex=_this.SelIndex<_this.ReData.length-1?_this.SelIndex+1:0;
					break;
			default:
					_this.SelIndex=0;
		}
		if(_this.Listeners){
			clearInterval(_this.Listeners);
		}
		_this.Listeners=setInterval(function(){
			_this.Get(ev.keyCode);
		},10);
	},
	//
	Get:function(keyCode){
		var _this=this;
		//иооб╪Э
		if(keyCode == 38 || keyCode == 40) {
			var oLi=_this.TgList.getElementsByTagName('li');
			for(var i = 0; i < oLi.length; i++) {
				oLi[i].style.cssText='background:#fff;';
			}
			oLi[_this.SelIndex].style.cssText='background:#36c;color:#fff;font-weight:bold;';
			return;
		} 
		if(_this.Target.value==''){
			_this.Clear();
			return;
		}
		if(_this.Listeners){
			clearInterval(_this.Listeners);
		};
		_this.ReData=[];
		var cResult='';
		GewaraUtil.sendRequest(_this.url, _this.prama(), function(result){
			_this.Data = result.rdata;
	    	for(var i=0;i<_this.Data.length;i++){
	    		_this.ReData.push(_this.Data[i]);
			}
			for(var i=0;i<_this.ReData.length;i++){
				cResult += '<li>' + _this.returnName(_this.ReData[i]) + '</li>';
			}
			if(cResult==''){
				_this.Clear();
				_this.ValTarget.value = "";
			}else{
				_this.TgList.innerHTML=cResult;
				_this.TgList.style.cssText='display:block;position:absolute;z-index:10;background: #fff;line-height: 25px;border-left: 1px solid; border-right: 1px solid; border-bottom: 1px solid;';
				_this.TgList.style.top=mSift_SeekTp(_this.Target,2)+'px';
				_this.TgList.style.left=mSift_SeekTp(_this.Target,3)+'px';
				_this.TgList.style.width=(_this.Target.offsetWidth - 2)+'px';
			}
			var oLi=_this.TgList.getElementsByTagName('li');
			if(oLi.length>0){
				oLi[_this.SelIndex].style.cssText='background:#36c;color:#fff;font-weight:bold;';
			}
		});
	},
	
	Clear:function(){
		var _this=this;
		if(_this.TgList){
			_this.TgList.style.display='none';
			_this.ReData=[];
			_this.SelIndex=0;
		}
	}
}

function mSift_SeekTp(oObj,nDire){
	if(oObj.getBoundingClientRect&&!document.all){
		var oDc=document.documentElement;
		switch(nDire){
			case 0:return oObj.getBoundingClientRect().topoDc.scrollTop;
			case 1:return oObj.getBoundingClientRect().right+oDc.scrollLeft;
			case 2:return oObj.offsetTop + oObj.offsetHeight+oDc.scrollTop;
			case 3:return oObj.offsetLeft+oDc.scrollLeft;
		}
	}else{
		if(nDire==1||nDire==3){
			var nPosition=oObj.offsetLeft;
		}else{
			var nPosition=oObj.offsetTop;
		}
		if(arguments[arguments.length-1]!=0){
			if(nDire==1){
				nPosition+=oObj.offsetWidth;
			}else if(nDire==2){
				nPosition+=oObj.offsetHeight;
			}
		}
		if(oObj.offsetParent!=null){
			nPosition+=mSift_SeekTp(oObj.offsetParent,nDire,0);
		}
		return nPosition;
	}
}
