from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Mapping as _Mapping, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class AlgoInput(_message.Message):
    __slots__ = ["iargs"]
    class IargsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    IARGS_FIELD_NUMBER: _ClassVar[int]
    iargs: _containers.ScalarMap[str, str]
    def __init__(self, iargs: _Optional[_Mapping[str, str]] = ...) -> None: ...

class AlgoOutput(_message.Message):
    __slots__ = ["oargs"]
    class OargsEntry(_message.Message):
        __slots__ = ["key", "value"]
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    OARGS_FIELD_NUMBER: _ClassVar[int]
    oargs: _containers.ScalarMap[str, str]
    def __init__(self, oargs: _Optional[_Mapping[str, str]] = ...) -> None: ...
