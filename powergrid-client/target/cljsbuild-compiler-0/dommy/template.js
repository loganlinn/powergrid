goog.provide('dommy.template');
goog.require('cljs.core');
goog.require('dommy.attrs');
goog.require('clojure.string');
dommy.template.PElement = {};
dommy.template._elem = (function _elem(this$){
if((function (){var and__3822__auto__ = this$;
if(and__3822__auto__)
{return this$.dommy$template$PElement$_elem$arity$1;
} else
{return and__3822__auto__;
}
})())
{return this$.dommy$template$PElement$_elem$arity$1(this$);
} else
{var x__2443__auto__ = (((this$ == null))?null:this$);
return (function (){var or__3824__auto__ = (dommy.template._elem[goog.typeOf(x__2443__auto__)]);
if(or__3824__auto__)
{return or__3824__auto__;
} else
{var or__3824__auto____$1 = (dommy.template._elem["_"]);
if(or__3824__auto____$1)
{return or__3824__auto____$1;
} else
{throw cljs.core.missing_protocol.call(null,"PElement.-elem",this$);
}
}
})().call(null,this$);
}
});
/**
* index of css character (#,.) in base-element. bottleneck
*/
dommy.template.next_css_index = (function next_css_index(s,start_idx){
var id_idx = s.indexOf("#",start_idx);
var class_idx = s.indexOf(".",start_idx);
var idx = Math.min(id_idx,class_idx);
if((idx < 0))
{return Math.max(id_idx,class_idx);
} else
{return idx;
}
});
/**
* dom element from css-style keyword like :a.class1 or :span#my-span.class
*/
dommy.template.base_element = (function base_element(node_key){
var node_str = cljs.core.name.call(null,node_key);
var base_idx = dommy.template.next_css_index.call(null,node_str,0);
var tag = (((base_idx > 0))?node_str.substring(0,base_idx):(((base_idx === 0))?"div":(("\uFDD0'else")?node_str:null)));
var node = document.createElement(tag);
if((base_idx >= 0))
{var str_5079 = node_str.substring(base_idx);
while(true){
var next_idx_5080 = dommy.template.next_css_index.call(null,str_5079,1);
var frag_5081 = (((next_idx_5080 >= 0))?str_5079.substring(0,next_idx_5080):str_5079);
var G__5078_5082 = frag_5081.charAt(0);
if(cljs.core._EQ_.call(null,"#",G__5078_5082))
{node.setAttribute("id",frag_5081.substring(1));
} else
{if(cljs.core._EQ_.call(null,".",G__5078_5082))
{dommy.attrs.add_class_BANG_.call(null,node,frag_5081.substring(1));
} else
{if("\uFDD0'else")
{throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(frag_5081.charAt(0))].join('')));
} else
{}
}
}
if((next_idx_5080 >= 0))
{{
var G__5083 = str_5079.substring(next_idx_5080);
str_5079 = G__5083;
continue;
}
} else
{}
break;
}
} else
{}
return node;
});
dommy.template.throw_unable_to_make_node = (function throw_unable_to_make_node(node_data){
throw [cljs.core.str("Don't know how to make node from: "),cljs.core.str(cljs.core.pr_str.call(null,node_data))].join('');
});
/**
* take data and return a document fragment
*/
dommy.template.__GT_document_fragment = (function() {
var __GT_document_fragment = null;
var __GT_document_fragment__1 = (function (data){
return __GT_document_fragment.call(null,document.createDocumentFragment(),data);
});
var __GT_document_fragment__2 = (function (result_frag,data){
if((function (){var G__5086 = data;
if(G__5086)
{if(cljs.core.truth_((function (){var or__3824__auto__ = null;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return G__5086.dommy$template$PElement$;
}
})()))
{return true;
} else
{if((!G__5086.cljs$lang$protocol_mask$partition$))
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5086);
} else
{return false;
}
}
} else
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5086);
}
})())
{result_frag.appendChild(dommy.template._elem.call(null,data));
return result_frag;
} else
{if(cljs.core.seq_QMARK_.call(null,data))
{var G__5087_5088 = cljs.core.seq.call(null,data);
while(true){
if(G__5087_5088)
{var child_5089 = cljs.core.first.call(null,G__5087_5088);
__GT_document_fragment.call(null,result_frag,child_5089);
{
var G__5090 = cljs.core.next.call(null,G__5087_5088);
G__5087_5088 = G__5090;
continue;
}
} else
{}
break;
}
return result_frag;
} else
{if((data == null))
{return result_frag;
} else
{if("\uFDD0'else")
{return dommy.template.throw_unable_to_make_node.call(null,data);
} else
{return null;
}
}
}
}
});
__GT_document_fragment = function(result_frag,data){
switch(arguments.length){
case 1:
return __GT_document_fragment__1.call(this,result_frag);
case 2:
return __GT_document_fragment__2.call(this,result_frag,data);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
__GT_document_fragment.cljs$lang$arity$1 = __GT_document_fragment__1;
__GT_document_fragment.cljs$lang$arity$2 = __GT_document_fragment__2;
return __GT_document_fragment;
})()
;
/**
* take data and return DOM node if it satisfies PElement and tries to
* make a document fragment otherwise
*/
dommy.template.__GT_node_like = (function __GT_node_like(data){
if((function (){var G__5092 = data;
if(G__5092)
{if(cljs.core.truth_((function (){var or__3824__auto__ = null;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return G__5092.dommy$template$PElement$;
}
})()))
{return true;
} else
{if((!G__5092.cljs$lang$protocol_mask$partition$))
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5092);
} else
{return false;
}
}
} else
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5092);
}
})())
{return dommy.template._elem.call(null,data);
} else
{return dommy.template.__GT_document_fragment.call(null,data);
}
});
/**
* element with either attrs or nested children [:div [:span "Hello"]]
*/
dommy.template.compound_element = (function compound_element(p__5093){
var vec__5100 = p__5093;
var tag_name = cljs.core.nth.call(null,vec__5100,0,null);
var maybe_attrs = cljs.core.nth.call(null,vec__5100,1,null);
var children = cljs.core.nthnext.call(null,vec__5100,2);
var n = dommy.template.base_element.call(null,tag_name);
var attrs = (((function (){var and__3822__auto__ = cljs.core.map_QMARK_.call(null,maybe_attrs);
if(and__3822__auto__)
{return !((function (){var G__5101 = maybe_attrs;
if(G__5101)
{if(cljs.core.truth_((function (){var or__3824__auto__ = null;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return G__5101.dommy$template$PElement$;
}
})()))
{return true;
} else
{if((!G__5101.cljs$lang$protocol_mask$partition$))
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5101);
} else
{return false;
}
}
} else
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5101);
}
})());
} else
{return and__3822__auto__;
}
})())?maybe_attrs:null);
var children__$1 = (cljs.core.truth_(attrs)?children:cljs.core.cons.call(null,maybe_attrs,children));
var G__5102_5106 = cljs.core.seq.call(null,attrs);
while(true){
if(G__5102_5106)
{var vec__5103_5107 = cljs.core.first.call(null,G__5102_5106);
var k_5108 = cljs.core.nth.call(null,vec__5103_5107,0,null);
var v_5109 = cljs.core.nth.call(null,vec__5103_5107,1,null);
var G__5104_5110 = k_5108;
if(cljs.core._EQ_.call(null,"\uFDD0'classes",G__5104_5110))
{var G__5105_5111 = cljs.core.seq.call(null,v_5109);
while(true){
if(G__5105_5111)
{var c_5112 = cljs.core.first.call(null,G__5105_5111);
dommy.attrs.add_class_BANG_.call(null,n,c_5112);
{
var G__5113 = cljs.core.next.call(null,G__5105_5111);
G__5105_5111 = G__5113;
continue;
}
} else
{}
break;
}
} else
{if(cljs.core._EQ_.call(null,"\uFDD0'class",G__5104_5110))
{dommy.attrs.add_class_BANG_.call(null,n,v_5109);
} else
{if("\uFDD0'else")
{dommy.attrs.set_attr_BANG_.call(null,n,k_5108,v_5109);
} else
{}
}
}
{
var G__5114 = cljs.core.next.call(null,G__5102_5106);
G__5102_5106 = G__5114;
continue;
}
} else
{}
break;
}
n.appendChild(dommy.template.__GT_node_like.call(null,children__$1));
return n;
});
(dommy.template.PElement["string"] = true);
(dommy.template._elem["string"] = (function (this$){
if(cljs.core.keyword_QMARK_.call(null,this$))
{return dommy.template.base_element.call(null,this$);
} else
{return document.createTextNode([cljs.core.str(this$)].join(''));
}
}));
(dommy.template.PElement["number"] = true);
(dommy.template._elem["number"] = (function (this$){
return document.createTextNode([cljs.core.str(this$)].join(''));
}));
cljs.core.PersistentVector.prototype.dommy$template$PElement$ = true;
cljs.core.PersistentVector.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return dommy.template.compound_element.call(null,this$);
});
Document.prototype.dommy$template$PElement$ = true;
Document.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return this$;
});
Text.prototype.dommy$template$PElement$ = true;
Text.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return this$;
});
DocumentFragment.prototype.dommy$template$PElement$ = true;
DocumentFragment.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return this$;
});
HTMLElement.prototype.dommy$template$PElement$ = true;
HTMLElement.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return this$;
});
try{Window.prototype.dommy$template$PElement$ = true;
Window.prototype.dommy$template$PElement$_elem$arity$1 = (function (this$){
return this$;
});
}catch (e5115){if(cljs.core.instance_QMARK_.call(null,ReferenceError,e5115))
{var __5116 = e5115;
console.log("PElement: js/Window not defined by browser, skipping it... (running on phantomjs?)");
} else
{if("\uFDD0'else")
{throw e5115;
} else
{}
}
}dommy.template.node = (function node(data){
if((function (){var G__5118 = data;
if(G__5118)
{if(cljs.core.truth_((function (){var or__3824__auto__ = null;
if(cljs.core.truth_(or__3824__auto__))
{return or__3824__auto__;
} else
{return G__5118.dommy$template$PElement$;
}
})()))
{return true;
} else
{if((!G__5118.cljs$lang$protocol_mask$partition$))
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5118);
} else
{return false;
}
}
} else
{return cljs.core.type_satisfies_.call(null,dommy.template.PElement,G__5118);
}
})())
{return dommy.template._elem.call(null,data);
} else
{return dommy.template.throw_unable_to_make_node.call(null,data);
}
});
dommy.template.html__GT_nodes = (function html__GT_nodes(html){
var parent = document.createElement("div");
parent.insertAdjacentHTML("beforeend",html);
return cljs.core.seq.call(null,Array.prototype.slice.call(parent.childNodes));
});
