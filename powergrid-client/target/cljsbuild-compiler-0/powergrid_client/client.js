goog.provide('powergrid_client.client');
goog.require('cljs.core');
goog.require('clojure.browser.repl');
goog.require('powergrid_client.power_plants');
goog.require('dommy.core');
powergrid_client.client.player_tpl = (function player_tpl(p__6256){
var map__6262 = p__6256;
var map__6262__$1 = ((cljs.core.seq_QMARK_.call(null,map__6262))?cljs.core.apply.call(null,cljs.core.hash_map,map__6262):map__6262);
var money = cljs.core._lookup.call(null,map__6262__$1,"\uFDD0'money",null);
var color = cljs.core._lookup.call(null,map__6262__$1,"\uFDD0'color",null);
var username = cljs.core._lookup.call(null,map__6262__$1,"\uFDD0'username",null);
var id = cljs.core._lookup.call(null,map__6262__$1,"\uFDD0'id",null);
var dom6263 = document.createElement("div");
dom6263.className = "player";
if(cljs.core.truth_([cljs.core.str("player-"),cljs.core.str(cljs.core.name.call(null,color))].join('')))
{dom6263.className = [cljs.core.str(dom6263.className),cljs.core.str(" "),cljs.core.str([cljs.core.str("player-"),cljs.core.str(cljs.core.name.call(null,color))].join(''))].join('').trim();
} else
{}
if(cljs.core.truth_([cljs.core.str("player-"),cljs.core.str(id)].join('')))
{dom6263.setAttribute("id",[cljs.core.str("player-"),cljs.core.str(id)].join(''));
} else
{}
dom6263.appendChild((function (){var dom6264 = document.createElement("span");
dom6264.className = "name";
dom6264.appendChild(dommy.template.__GT_node_like.call(null,username));
return dom6264;
})());
dom6263.appendChild((function (){var dom6265 = document.createElement("div");
dom6265.className = "money";
dom6265.appendChild(dommy.template.__GT_node_like.call(null,money));
return dom6265;
})());
dom6263.appendChild((function (){var dom6266 = document.createElement("div");
dom6266.className = "power-plants";
dom6266.appendChild(document.createTextNode(""));
return dom6266;
})());
return dom6263;
});
powergrid_client.client.resources_tpl = (function resources_tpl(){
var dom6272 = document.createElement("div");
dom6272.setAttribute("id","resources");
dom6272.appendChild(dommy.template.__GT_node_like.call(null,cljs.core.concat.call(null,(function (){var iter__2540__auto__ = (function iter__6273(s__6274){
return (new cljs.core.LazySeq(null,false,(function (){
var s__6274__$1 = s__6274;
while(true){
if(cljs.core.seq.call(null,s__6274__$1))
{var cost = cljs.core.first.call(null,s__6274__$1);
return cljs.core.cons.call(null,cljs.core.PersistentVector.fromArray(["\uFDD0'li",cljs.core.ObjMap.fromObject(["\uFDD0'data-resource-cost"],{"\uFDD0'data-resource-cost":cost}),cljs.core.PersistentVector.fromArray(["\uFDD0'div",cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-coal"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-coal"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-coal"], true)], true),cljs.core.PersistentVector.fromArray(["\uFDD0'div",cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-oil"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-oil"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-oil"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-uranium"], true)], true),cljs.core.PersistentVector.fromArray(["\uFDD0'div",cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-garbage"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-garbage"], true),cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-garbage"], true)], true)], true),iter__6273.call(null,cljs.core.rest.call(null,s__6274__$1)));
} else
{return null;
}
break;
}
}),null));
});
return iter__2540__auto__.call(null,cljs.core.range.call(null,1,9));
})(),(function (){var iter__2540__auto__ = (function iter__6275(s__6276){
return (new cljs.core.LazySeq(null,false,(function (){
var s__6276__$1 = s__6276;
while(true){
if(cljs.core.seq.call(null,s__6276__$1))
{var cost = cljs.core.first.call(null,s__6276__$1);
return cljs.core.cons.call(null,cljs.core.PersistentVector.fromArray(["\uFDD0'li",cljs.core.ObjMap.fromObject(["\uFDD0'data-resource-cost"],{"\uFDD0'data-resource-cost":cost}),cljs.core.PersistentVector.fromArray(["\uFDD0'div",cljs.core.PersistentVector.fromArray(["\uFDD0'span.resource-uranium"], true)], true)], true),iter__6275.call(null,cljs.core.rest.call(null,s__6276__$1)));
} else
{return null;
}
break;
}
}),null));
});
return iter__2540__auto__.call(null,cljs.core.range.call(null,10,18,2));
})())));
return dom6272;
});
powergrid_client.client.resource_name = (function resource_name(r){
if(cljs.core.set_QMARK_.call(null,r))
{return clojure.string.join.call(null,"-",cljs.core.sort.call(null,cljs.core.map.call(null,cljs.core.name,powergrid_client.client.resource)));
} else
{return cljs.core.name.call(null,r);
}
});
powergrid_client.client.power_plant_tpl = (function power_plant_tpl(p__6277){
var map__6283 = p__6277;
var map__6283__$1 = ((cljs.core.seq_QMARK_.call(null,map__6283))?cljs.core.apply.call(null,cljs.core.hash_map,map__6283):map__6283);
var yield$ = cljs.core._lookup.call(null,map__6283__$1,"\uFDD0'yield",null);
var capacity = cljs.core._lookup.call(null,map__6283__$1,"\uFDD0'capacity",null);
var resource = cljs.core._lookup.call(null,map__6283__$1,"\uFDD0'resource",null);
var number = cljs.core._lookup.call(null,map__6283__$1,"\uFDD0'number",null);
var dom6284 = document.createElement("div");
dom6284.className = "power-plant";
if(cljs.core.truth_(cljs.core.format.call(null,"resource-%s power-plant-%d",powergrid_client.client.resource_name.call(null,resource),number)))
{dom6284.className = [cljs.core.str(dom6284.className),cljs.core.str(" "),cljs.core.str(cljs.core.format.call(null,"resource-%s power-plant-%d",powergrid_client.client.resource_name.call(null,resource),number))].join('').trim();
} else
{}
dom6284.appendChild((function (){var dom6285 = document.createElement("span");
dom6285.className = "number";
dom6285.appendChild(dommy.template.__GT_node_like.call(null,number));
return dom6285;
})());
dom6284.appendChild((function (){var dom6286 = document.createElement("span");
dom6286.className = "capacity";
dom6286.appendChild(dommy.template.__GT_node_like.call(null,capacity));
return dom6286;
})());
dom6284.appendChild((function (){var dom6287 = document.createElement("span");
dom6287.className = "yield";
dom6287.appendChild(dommy.template.__GT_node_like.call(null,yield$));
return dom6287;
})());
return dom6284;
});
powergrid_client.client.power_plants_tpl = (function power_plants_tpl(power_plants){
var dom6291 = document.createElement("div");
dom6291.setAttribute("id","power-plants");
dom6291.appendChild((function (){var dom6292 = document.createElement("div");
dom6292.className = "market";
dom6292.appendChild(dommy.template.__GT_node_like.call(null,cljs.core.map.call(null,powergrid_client.client.power_plant_tpl,(new cljs.core.Keyword("\uFDD0'market")).call(null,power_plants))));
return dom6292;
})());
dom6291.appendChild((function (){var dom6293 = document.createElement("div");
dom6293.className = "future";
dom6293.appendChild(dommy.template.__GT_node_like.call(null,cljs.core.map.call(null,powergrid_client.client.power_plant_tpl,(new cljs.core.Keyword("\uFDD0'future")).call(null,power_plants))));
return dom6293;
})());
return dom6291;
});
powergrid_client.client.game_tpl = (function game_tpl(p__6294){
var map__6298 = p__6294;
var map__6298__$1 = ((cljs.core.seq_QMARK_.call(null,map__6298))?cljs.core.apply.call(null,cljs.core.hash_map,map__6298):map__6298);
var power_plants = cljs.core._lookup.call(null,map__6298__$1,"\uFDD0'power-plants",null);
var players = cljs.core._lookup.call(null,map__6298__$1,"\uFDD0'players",null);
var dom6299 = document.createElement("div");
dom6299.setAttribute("id","game");
dom6299.appendChild((function (){var dom6300 = document.createElement("div");
dom6300.setAttribute("id","players");
dom6300.appendChild(dommy.template.__GT_node_like.call(null,cljs.core.map.call(null,powergrid_client.client.player_tpl,cljs.core.vals.call(null,players))));
return dom6300;
})());
dom6299.appendChild(dommy.template.__GT_node_like.call(null,powergrid_client.client.resources_tpl.call(null)));
dom6299.appendChild(dommy.template.__GT_node_like.call(null,powergrid_client.client.power_plants_tpl.call(null,power_plants)));
return dom6299;
});
var std_pricing_6305 = (function (){var iter__2540__auto__ = (function iter__6301(s__6302){
return (new cljs.core.LazySeq(null,false,(function (){
var s__6302__$1 = s__6302;
while(true){
if(cljs.core.seq.call(null,s__6302__$1))
{var p = cljs.core.first.call(null,s__6302__$1);
var iterys__2538__auto__ = ((function (s__6302__$1,p){
return (function iter__6303(s__6304){
return (new cljs.core.LazySeq(null,false,((function (s__6302__$1,p){
return (function (){
var s__6304__$1 = s__6304;
while(true){
if(cljs.core.seq.call(null,s__6304__$1))
{var _ = cljs.core.first.call(null,s__6304__$1);
return cljs.core.cons.call(null,p,iter__6303.call(null,cljs.core.rest.call(null,s__6304__$1)));
} else
{return null;
}
break;
}
});})(s__6302__$1,p))
,null));
});})(s__6302__$1,p))
;
var fs__2539__auto__ = cljs.core.seq.call(null,iterys__2538__auto__.call(null,cljs.core.range.call(null,3)));
if(fs__2539__auto__)
{return cljs.core.concat.call(null,fs__2539__auto__,iter__6301.call(null,cljs.core.rest.call(null,s__6302__$1)));
} else
{{
var G__6307 = cljs.core.rest.call(null,s__6302__$1);
s__6302__$1 = G__6307;
continue;
}
}
} else
{return null;
}
break;
}
}),null));
});
return iter__2540__auto__.call(null,cljs.core.range.call(null,1,9));
})();
var uranium_pricing_6306 = cljs.core.with_meta(cljs.core.list(1,2,3,4,5,6,7,8,12,14,15,16),cljs.core.hash_map("\uFDD0'line",66));
powergrid_client.client.mock_game = cljs.core.ObjMap.fromObject(["\uFDD0'players","\uFDD0'resources","\uFDD0'power-plants"],{"\uFDD0'players":cljs.core.PersistentArrayMap.fromArrays([1,2],[cljs.core.ObjMap.fromObject(["\uFDD0'id","\uFDD0'username","\uFDD0'color","\uFDD0'money","\uFDD0'power-plants"],{"\uFDD0'id":1,"\uFDD0'username":"Logan","\uFDD0'color":"\uFDD0'red","\uFDD0'money":50,"\uFDD0'power-plants":cljs.core.ObjMap.EMPTY}),cljs.core.ObjMap.fromObject(["\uFDD0'id","\uFDD0'username","\uFDD0'color","\uFDD0'money","\uFDD0'power-plants"],{"\uFDD0'id":2,"\uFDD0'username":"Maeby","\uFDD0'color":"\uFDD0'black","\uFDD0'money":50,"\uFDD0'power-plants":cljs.core.ObjMap.EMPTY})]),"\uFDD0'resources":cljs.core.ObjMap.fromObject(["\uFDD0'coal","\uFDD0'oil","\uFDD0'garbage","\uFDD0'uranium"],{"\uFDD0'coal":cljs.core.ObjMap.fromObject(["\uFDD0'market","\uFDD0'supply","\uFDD0'pricing"],{"\uFDD0'market":24,"\uFDD0'supply":0,"\uFDD0'pricing":std_pricing_6305}),"\uFDD0'oil":cljs.core.ObjMap.fromObject(["\uFDD0'market","\uFDD0'supply","\uFDD0'pricing"],{"\uFDD0'market":18,"\uFDD0'supply":6,"\uFDD0'pricing":std_pricing_6305}),"\uFDD0'garbage":cljs.core.ObjMap.fromObject(["\uFDD0'market","\uFDD0'supply","\uFDD0'pricing"],{"\uFDD0'market":6,"\uFDD0'supply":18,"\uFDD0'pricing":std_pricing_6305}),"\uFDD0'uranium":cljs.core.ObjMap.fromObject(["\uFDD0'market","\uFDD0'supply","\uFDD0'pricing"],{"\uFDD0'market":2,"\uFDD0'supply":10,"\uFDD0'pricing":uranium_pricing_6306})}),"\uFDD0'power-plants":cljs.core.ObjMap.fromObject(["\uFDD0'market","\uFDD0'future","\uFDD0'deck"],{"\uFDD0'market":powergrid_client.power_plants.initial_market.call(null),"\uFDD0'future":powergrid_client.power_plants.initial_future.call(null),"\uFDD0'deck":powergrid_client.power_plants.initial_deck.call(null)})});
dommy.core.replace_BANG_.call(null,document.getElementById("game"),powergrid_client.client.game_tpl.call(null,powergrid_client.client.mock_game));
dommy.core.listen_BANG_.call(null,document.getElementById("clickable"),"\uFDD0'click",(function (){
return alert("Hello!");
}));
