
class KosmosClampFloat(float):
    ##some magic to make KosmosClamp behave like an int :)
    def __new__(cls, min, max, step, value):
        return float.__new__(cls, value)

    def __init__(self, min=0, max=255, step=1, value=None):
        self.min = min
        self.max = max
        self.step = step
        if value is not None:
            self.value = self._clamp(value)
        else:
            self.value = float(min)

    def _clamp(self, input):
        return float(max(min(input, self.max), self.min))

    def set(self, input):
        self.value = self._clamp(input)

    def _change(self, amount):
        self.value = self._clamp(self.value + amount)
        return self.value

    def increase(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(amount)

    def decrease(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(-amount)

    def current(self):
        return self.value

    def __repr__(self):
        return self.value

    def __str__(self):
        return str(self.value)

    def __isub__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, self.value - amount)

    def __iadd__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, self.value + amount)

    def __imul__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value * amount))

    def __rtruediv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __itruediv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __rfloordiv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __ifloordiv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))


class KosmosClampInt(int):
    ##some magic to make KosmosClamp behave like an int :)
    def __new__(cls, min, max, step, value):
        return int.__new__(cls, value)

    def __init__(self, min=0, max=255, step=1, value=None):

        self.min = min
        self.max = max
        self.step = step
        if value is not None:
            self.value = self._clamp(value)
        else:
            self.value = int(min)

    def _clamp(self, input):
        return int(max(min(input, self.max), self.min))

    def set(self, input):
        self.value = self._clamp(input)

    def _change(self, amount):
        self.value = self._clamp(self.value + amount)
        return self.value

    def increase(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(amount)

    def decrease(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(-amount)

    def current(self):
        return self.value

    def __repr__(self):
        return self.value

    def __str__(self):
        return str(self.value)

    def __isub__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, self.value - amount)

    def __iadd__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, self.value + amount)

    def __imul__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value * amount))

    def __rtruediv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __itruediv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __rfloordiv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __ifloordiv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))


class KosmosList:
    #    __metaclass__ = ListMeta # We need that meta class...
    def __init__(self, entries, maxLen=None):
        self.entries = entries
        self.myIndex = None
        self.maxLen = maxLen

    def set_index(self, index):
        self.myIndex = index % len(self.entries)

    def next(self):
        if self.myIndex is None:
            self.myIndex = -1

        self.set_index(self.myIndex + 1)
        return self.entries[self.myIndex]

    def prev(self):
        if self.myIndex is None:
            self.myIndex = 0
        self.set_index(self.myIndex - 1)
        return self.entries[self.myIndex]

    def curr(self):
        if self.myIndex is None:
            self.myIndex = 0
        return self.entries[self.myIndex]

    def add(self, item):
        if self.maxLen is not None:
            while len(self.entries) >= self.maxLen:
                self.entries.pop(0)
        self.entries.append(item)

    def __iter__(self):
        ''' Returns the Iterator object '''
        return iter(self.entries)

    def __len__(self):
        return len(self.entries)

    def __getitem__(self, index):
        return self.entries[index]

    def index(self, key):
        return self.entries.index(key)

